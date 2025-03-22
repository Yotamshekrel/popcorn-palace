package com.att.tdp.popcorn_palace.integration;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This test starts the full application on a random port
 * and uses RestTemplate to send real HTTP calls.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional // Each test method is wrapped in a transaction
class MovieIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    MovieRepository movieRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        movieRepository.deleteAll();
    }

    @Test
    @DisplayName("Full create -> get -> delete flow")
    void testCreateGetDeleteFlow() {
        // 1. GET the initial count of movies
        URI getUri = URI.create("http://localhost:" + port + "/movies/all");
        ResponseEntity<Movie[]> getAllResp = restTemplate.getForEntity(getUri, Movie[].class);
        int initialCount = getAllResp.getBody().length;
       
        // 2. CREATE a new movie
        URI uri = URI.create("http://localhost:" + port + "/movies");
        String requestBody = """
            {
              "title": "IntegrationMovie",
              "genre": "Adventure",
              "duration": 120,
              "rating": 8.0,
              "releaseYear": 2010
            }
        """;
        // 3. POST /movies with the new movie data
        ResponseEntity<String> createResp = restTemplate.postForEntity(uri,
                new HttpEntity<>(requestBody, createJsonHeaders()), String.class);

        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResp.getBody()).contains("Successfully created the movie");

        // 4. GET /movies/all to confirm it's present
         getUri = URI.create("http://localhost:" + port + "/movies/all");
         getAllResp = restTemplate.getForEntity(getUri, Movie[].class);

        assertThat(getAllResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        Movie[] movies = getAllResp.getBody();
        assertThat(movies).isNotNull();
        assertThat(movies.length).isEqualTo(initialCount + 1);
        assertThat(movies[initialCount].getTitle()).isEqualTo("IntegrationMovie");
        
        // 5. UPDATE /movies/IntegrationMovie to change its data
        URI updateUri = URI.create("http://localhost:" + port + "/movies/update/IntegrationMovie");
        String updateRequestBody = """
            {
              "title": "UpdatedIntegrationMovie",
              "genre": "Action",
              "duration": 130,
              "rating": 9.0,
              "releaseYear": 2012
            }
        """;
        ResponseEntity<String> updateResp = restTemplate.exchange(updateUri, HttpMethod.POST,
                new HttpEntity<>(updateRequestBody, createJsonHeaders()), String.class);

        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody()).contains("Movie 'IntegrationMovie' was updated successfully. New title is 'UpdatedIntegrationMovie");

        // Confirm the update
        getUri = URI.create("http://localhost:" + port + "/movies/all");
        getAllResp = restTemplate.getForEntity(getUri, Movie[].class);

        Movie updatedMovie = getAllResp.getBody()[initialCount];
        assertThat(updatedMovie).isNotNull();
        assertThat(updatedMovie.getTitle()).isEqualTo("UpdatedIntegrationMovie");
        assertThat(movieRepository.existsByTitle("IntegrationMovie")).isFalse();
        assertThat(updatedMovie.getGenre()).isEqualTo("Action");
        assertThat(updatedMovie.getDuration()).isEqualTo(130);
        assertThat(updatedMovie.getRating()).isEqualTo(9.0);
        assertThat(updatedMovie.getReleaseYear()).isEqualTo(2012);

        // 6. DELETE /movies/IntegrationMovie
        URI deleteUri = URI.create("http://localhost:" + port + "/movies/UpdatedIntegrationMovie");
        restTemplate.delete(deleteUri);

        // 7. Ensure it's gone
        assertThat(movieRepository.existsByTitle("UpdatedIntegrationMovie")).isFalse();
    }

    // Helper: standard JSON headers
    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
