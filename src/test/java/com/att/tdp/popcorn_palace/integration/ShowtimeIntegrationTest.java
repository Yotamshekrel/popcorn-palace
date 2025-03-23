package com.att.tdp.popcorn_palace.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.showtime.Showtime;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ShowtimeIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    ShowtimeRepository showtimeRepository;

    @Autowired
    MovieRepository movieRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        showtimeRepository.deleteAll();
        movieRepository.deleteAll();
    }

    @Test
    @Rollback
    @DisplayName("Full create -> get -> update -> delete flow for showtime")
    void testFullFlow() {
        // Insert movie
        Long movieId = insertTestMovie();

        // 1) Create
        URI createUri = URI.create("http://localhost:" + port + "/showtimes");
        String createBody = String.format("""
                    {
                      "movieId": %d,
                      "theater": "IntegrationTestTheater",
                      "startTime": "2025-04-01T14:00:00",
                      "endTime": "2025-04-01T16:00:00",
                      "price": 12.0
                    }
                """, movieId);

        ResponseEntity<String> createResp = restTemplate.postForEntity(
                createUri,
                new HttpEntity<>(createBody, createJsonHeaders()),
                String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResp.getBody()).contains("Successfully created showtime");

        // 2) Get (GET /showtimes)
        Showtime st = showtimeRepository.findAll().get(0);
        assertThat(st.getTheater()).isEqualTo("IntegrationTestTheater");

        // 3) Update (POST /showtimes/update/{id})
        URI updateUri = URI.create("http://localhost:" + port + "/showtimes/update/" + st.getId());
        String updateBody = String.format("""
                    {
                      "movieId": %d,
                      "theater": "IntegrationTestTheater - Updated",
                      "startTime": "2025-04-01T14:00:00",
                      "endTime": "2025-04-01T16:00:00",
                      "price": 15.00
                    }
                """, movieId);
        ResponseEntity<String> updateResp = restTemplate.exchange(
                updateUri,
                HttpMethod.POST,
                new HttpEntity<>(updateBody, createJsonHeaders()),
                String.class);
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody()).contains("updated successfully");

        // 4) Verify update
        Showtime updated = showtimeRepository.findById(st.getId()).orElseThrow();

        assertThat(updated.getTheater()).contains("Updated");
        assertThat(updated.getPrice()).isEqualTo(BigDecimal.valueOf(15.00).setScale(2));

        // 5) Delete (DELETE /showtimes/{id})
        URI deleteUri = URI.create("http://localhost:" + port + "/showtimes/" + st.getId());
        restTemplate.delete(deleteUri);

        assertThat(showtimeRepository.existsById(st.getId())).isFalse();
    }

    @Test
    @Rollback
    @DisplayName("Should reject invalid data with 400 (endTime <= startTime)")
    void shouldRejectInvalidData() {
        // Create a movie
        Long movieId = insertTestMovie();

        // Create with endTime <= startTime
        URI createUri = URI.create("http://localhost:" + port + "/showtimes");
        String createBody = String.format("""
                    {
                      "movieId": %d,
                      "theater": "BadTime",
                      "startTime": "2025-04-01T15:00:00",
                      "endTime": "2025-04-01T15:00:00",
                      "price": 10.0
                    }
                """, movieId);

        // Expect a 400
        try {
            restTemplate.postForEntity(
                    createUri,
                    new HttpEntity<>(createBody, createJsonHeaders()),
                    String.class);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getResponseBodyAsString()).contains("endTime must be after startTime");
        }

    }

    @Test
    @DisplayName("Should reject overlapping showtime in same theater with 409")
    void shouldRejectOverlappingShowtime() {
        // Create a movie
        Long movieId = insertTestMovie();

        // Create a showtime
        URI uri = URI.create("http://localhost:" + port + "/showtimes");
        String body1 = String.format("""
                    {
                      "movieId": %d,
                      "theater": "OverlapTheater",
                      "startTime": "2025-04-01T14:00:00",
                      "endTime": "2025-04-01T16:00:00",
                      "price": 12.00
                    }
                """, movieId);

        ResponseEntity<String> createResp = restTemplate.postForEntity(
                uri,
                new HttpEntity<>(body1, createJsonHeaders()),
                String.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Now try to add an overlapping showtime
        String body2 = String.format("""
                    {
                      "movieId": %d,
                      "theater": "OverlapTheater",
                      "startTime": "2025-04-01T15:00:00",
                      "endTime": "2025-04-01T17:00:00",
                      "price": 15.00
                    }
                """, movieId);

        // Expect a 409
        try {
            restTemplate.postForEntity(
                    uri,
                    new HttpEntity<>(body2, createJsonHeaders()),
                    String.class);
            fail("Expected HttpClientErrorException to be thrown due to overlap");
        } catch (HttpClientErrorException.Conflict ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ex.getResponseBodyAsString()).contains("overlaps");
        }
    }

    @Test
    @DisplayName("Should fetch a showtime by ID")
    void shouldFetchShowtimeById() {
        // Create a movie
        Long movieId = insertTestMovie();

        // Create a showtime
        URI uri = URI.create("http://localhost:" + port + "/showtimes");
        String body = String.format("""
                    {
                    "movieId": %d,
                    "theater": "FetchTheater",
                    "startTime": "2025-04-01T13:00:00",
                    "endTime": "2025-04-01T15:00:00",
                    "price": 8.0
                    }
                """, movieId);

        restTemplate.postForEntity(
                uri,
                new HttpEntity<>(body, createJsonHeaders()),
                String.class);

        Showtime saved = showtimeRepository.findAll().get(0);

        // Fetch it by ID
        URI getUri = URI.create("http://localhost:" + port + "/showtimes/" + saved.getId());

        ResponseEntity<Showtime> resp = restTemplate.getForEntity(getUri, Showtime.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTheater()).isEqualTo("FetchTheater");
        assertThat(resp.getBody().getMovieId()).isEqualTo(movieId);
    }

    @Test
    @DisplayName("Should reject update with non-existent movie ID")
    void shouldRejectUpdateWithBadMovieId() {
        // Create a movie
        Long movieId = insertTestMovie();

        // Create a showtime
        URI uri = URI.create("http://localhost:" + port + "/showtimes");
        String body = String.format("""
                    {
                    "movieId": %d,
                    "theater": "UpdateTest",
                    "startTime": "2025-04-01T14:00:00",
                    "endTime": "2025-04-01T16:00:00",
                    "price": 11.0
                    }
                """, movieId);

        restTemplate.postForEntity(uri, new HttpEntity<>(body, createJsonHeaders()), String.class);
        Showtime st = showtimeRepository.findAll().get(0);

        // Attempt to update with a non-existent movie ID
        URI updateUri = URI.create("http://localhost:" + port + "/showtimes/update/" + st.getId());
        String badUpdate = """
                    {
                    "movieId": 9999,
                    "theater": "UpdateTest",
                    "startTime": "2025-04-01T14:00:00",
                    "endTime": "2025-04-01T16:00:00",
                    "price": 11.0
                    }
                """;

        // Expect a 400
        try {
            restTemplate.postForEntity(updateUri, new HttpEntity<>(badUpdate, createJsonHeaders()), String.class);
            fail("Expected error due to bad movie ID");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertThat(ex.getResponseBodyAsString()).contains("No movie found");
        }

    }

    // Helper to create JSON headers
    private HttpHeaders createJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    // Helper method to insert a test movie
    private Long insertTestMovie() {
        Movie movie = new Movie("Test Movie", "Action", 120, 7.5, 2024);
        movieRepository.save(movie);
        return movie.getId();
    }
}
