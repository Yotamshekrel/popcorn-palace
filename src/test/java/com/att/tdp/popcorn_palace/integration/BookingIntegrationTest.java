package com.att.tdp.popcorn_palace.integration;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.att.tdp.popcorn_palace.repository.BookingRepository;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
class BookingIntegrationTest {

    @LocalServerPort
    int port;

    @Autowired
    BookingRepository bookingRepository;

    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        bookingRepository.deleteAll();
    }

    
    @Test
    @DisplayName("Should create booking, then fail on seat conflict")
    void testCreateAndConflict() {
        // 1) Create a valid booking
        var createUri = URI.create("http://localhost:" + port + "/bookings");
        String body = """
            {
              "showtimeId": 1,
              "seatNumber": 50,
              "userId": "00000000-0000-0000-0000-000000000000"
            }
        """;
        ResponseEntity<String> resp = restTemplate.postForEntity(
            createUri,
            new HttpEntity<>(body, jsonHeaders()),
            String.class
        );
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).contains("bookingId");

        // 2) Try same seat
        try{
            restTemplate.postForEntity(
                createUri,
                new HttpEntity<>(body, jsonHeaders()),
                String.class
            );
            fail("Expected HttpClientErrorException");
        }
        catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(e.getResponseBodyAsString()).contains("Seat 50 is already booked for showtime 1");
        }
       
    }

    @Test
    @DisplayName("Should return 400 if userId is invalid")
    void testInvalidUserId() {
        var createUri = URI.create("http://localhost:" + port + "/bookings");
        String body = """
            {
              "showtimeId": 1,
              "seatNumber": 10,
              "userId": "NOT-A-UUID"
            }
        """;
        try{
            // Should fail with 400
        restTemplate.postForEntity(
            createUri,
            new HttpEntity<>(body, jsonHeaders()),
            String.class
        );
        fail("Expected HttpClientErrorException");
        }
        catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(e.getResponseBodyAsString()).contains("Invalid userId: must be a valid UUID");
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
