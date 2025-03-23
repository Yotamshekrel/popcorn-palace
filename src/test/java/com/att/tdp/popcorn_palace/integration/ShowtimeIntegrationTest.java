package com.att.tdp.popcorn_palace.integration;


import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.att.tdp.popcorn_palace.model.showtime.Showtime;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;

import java.math.BigDecimal;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * A full integration test using random port and real DB.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
// @Transactional
class ShowtimeIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    ShowtimeRepository showtimeRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        showtimeRepository.deleteAll();
    }

    @Test
    @Rollback
    @DisplayName("Full create -> get -> update -> delete flow for showtime")
    void testFullFlow() {
        // 1) Create
        URI createUri = URI.create("http://localhost:" + port + "/showtimes");
        String createBody = """
            {
              "movieId": 1,
              "theater": "IntegrationTestTheater",
              "startTime": "2025-04-01T14:00:00",
              "endTime": "2025-04-01T16:00:00",
              "price": 12.0
            }
        """;

        ResponseEntity<String> createResp = restTemplate.postForEntity(
            createUri,
            new HttpEntity<>(createBody, createJsonHeaders()),
            String.class
        );
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResp.getBody()).contains("Successfully created showtime");

        // 2) Let's see if it really got created
        // We know the DB is auto inc. Let's just pick up the showtime from DB:
        Showtime st = showtimeRepository.findAll().get(0);
        assertThat(st.getTheater()).isEqualTo("IntegrationTestTheater");

        // 3) Update (POST /showtimes/update/{id})
        URI updateUri = URI.create("http://localhost:" + port + "/showtimes/update/" + st.getId());
        String updateBody = """
            {
              "movieId": 1,
              "theater": "IntegrationTestTheater - Updated",
              "startTime": "2025-04-01T14:00:00",
              "endTime": "2025-04-01T16:00:00",
              "price": 15.00
            }
        """;
        ResponseEntity<String> updateResp = restTemplate.exchange(
        updateUri,
        HttpMethod.POST,
        new HttpEntity<>(updateBody, createJsonHeaders()),
        String.class
    );
        assertThat(updateResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updateResp.getBody()).contains("updated successfully");

        // 4) Confirm in DB
        Showtime updated = showtimeRepository.findById(st.getId()).orElseThrow();
        System.out.println("Updated theater = " + updated.getTheater());

        assertThat(updated.getTheater()).contains("Updated");
        assertThat(updated.getPrice()).isEqualTo(BigDecimal.valueOf(15.00).setScale(2));


        // 5) DELETE
        URI deleteUri = URI.create("http://localhost:" + port + "/showtimes/" + st.getId());
        restTemplate.delete(deleteUri);

        assertThat(showtimeRepository.existsById(st.getId())).isFalse();
    }

    @Test
    @Rollback
    @DisplayName("Should reject invalid data with 400 (endTime <= startTime)")
    void shouldRejectInvalidData() {
        URI createUri = URI.create("http://localhost:" + port + "/showtimes");
        String createBody = """
            {
              "movieId": 1,
              "theater": "BadTime",
              "startTime": "2025-04-01T15:00:00",
              "endTime": "2025-04-01T15:00:00",
              "price": 10.0
            }
        """;
        
        try {
            restTemplate.postForEntity(
                createUri,
                new HttpEntity<>(createBody, createJsonHeaders()),
                String.class
            );
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException.BadRequest ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(ex.getResponseBodyAsString()).contains("endTime must be after startTime");
        }

    }


    @Test
    @DisplayName("Should reject overlapping showtime in same theater with 409")
    void shouldRejectOverlappingShowtime() {
        // Create the first showtime
        URI uri = URI.create("http://localhost:" + port + "/showtimes");
        String body1 = """
            {
            "movieId": 1,
            "theater": "OverlapTheater",
            "startTime": "2025-04-01T14:00:00",
            "endTime": "2025-04-01T16:00:00",
            "price": 12.00
            }
        """;

        ResponseEntity<String> createResp = restTemplate.postForEntity(
            uri,
            new HttpEntity<>(body1, createJsonHeaders()),
            String.class
        );
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Now try to add an overlapping showtime
        String body2 = """
            {
            "movieId": 1,
            "theater": "OverlapTheater",
            "startTime": "2025-04-01T15:30:00",
            "endTime": "2025-04-01T17:00:00",
            "price": 10.00
            }
        """;

        try {
            restTemplate.postForEntity(
                uri,
                new HttpEntity<>(body2, createJsonHeaders()),
                String.class
            );
            fail("Expected HttpClientErrorException to be thrown due to overlap");
        } catch (HttpClientErrorException.Conflict ex) {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(ex.getResponseBodyAsString()).contains("overlaps");
        }}


    @Test
    @DisplayName("Should fetch a showtime by ID")
    void shouldFetchShowtimeById() {
        // Create a showtime
        URI uri = URI.create("http://localhost:" + port + "/showtimes");
        String body = """
            {
            "movieId": 1,
            "theater": "FetchTheater",
            "startTime": "2025-04-01T13:00:00",
            "endTime": "2025-04-01T15:00:00",
            "price": 8.0
            }
        """;

        restTemplate.postForEntity(
            uri,
            new HttpEntity<>(body, createJsonHeaders()),
            String.class
        );

        Showtime saved = showtimeRepository.findAll().get(0);

        // Fetch it by ID
        URI getUri = URI.create("http://localhost:" + port + "/showtimes/" + saved.getId());

        ResponseEntity<Showtime> resp = restTemplate.getForEntity(getUri, Showtime.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getTheater()).isEqualTo("FetchTheater");
        assertThat(resp.getBody().getMovieId()).isEqualTo(1L);
    }


    @Test
    @DisplayName("Should reject update with non-existent movie ID")
    void shouldRejectUpdateWithBadMovieId() {
        // Create valid showtime
        URI uri = URI.create("http://localhost:" + port + "/showtimes");
        String body = """
            {
            "movieId": 1,
            "theater": "UpdateTest",
            "startTime": "2025-04-01T14:00:00",
            "endTime": "2025-04-01T16:00:00",
            "price": 11.0
            }
        """;
        restTemplate.postForEntity(uri, new HttpEntity<>(body, createJsonHeaders()), String.class);
        Showtime st = showtimeRepository.findAll().get(0);

        // Update with non-existent movieId
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
}
