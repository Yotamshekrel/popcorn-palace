package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.model.booking.Booking;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /bookings")
    class PostBookings {

        @Test
        @DisplayName("Should book a valid seat (200 OK)")
        @Rollback
        void shouldBookValidSeat() throws Exception {
            // Insert a showtime
            String body = """
                {
                  "showtimeId": 1,
                  "seatNumber": 10,
                  "userId": "00000000-0000-0000-0000-000000000000"
                }
            """;

            mockMvc.perform(post("/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").exists()); 
        }

        @Test
        @DisplayName("Should return 400 if showtime does not exist")
        @Rollback
        void shouldReturnBadRequestIfShowtimeMissing() throws Exception {
            String body = """
                {
                  "showtimeId": 9999999,
                  "seatNumber": 5,
                  "userId": "11111111-1111-1111-1111-111111111111"
                }
            """;
            mockMvc.perform(post("/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No showtime found with id")));
        }

        @Test
        @DisplayName("Should return 409 if seat is already taken for that showtime")
        @Rollback
        void shouldReturnConflictForDoubleBooking() throws Exception {
            // Insert a booking
            Booking existing = new Booking(1L, UUID.randomUUID(), 15);
            bookingRepository.save(existing);

            // Try to book the same seat
            String body = """
                {
                  "showtimeId": 1,
                  "seatNumber": 15,
                  "userId": "22222222-2222-2222-2222-222222222222"
                }
            """;

            mockMvc.perform(post("/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Seat 15 is already booked")));
        }

        @Test
        @DisplayName("Should return 400 if seatNumber <= 0")
        @Rollback
        void shouldReturnBadRequestIfInvalidSeat() throws Exception {
            // Insert a showtime
            String body = """
                {
                  "showtimeId": 1,
                  "seatNumber": 0,
                  "userId": "33333333-3333-3333-3333-333333333333"
                }
            """;

            // Try to book an invalid seat
            mockMvc.perform(post("/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("seatNumber must be > 0")));
        }

        @Test
        @DisplayName("Should return 400 if userId is not a valid UUID")
        @Rollback
        void shouldReturnBadRequestIfInvalidUserId() throws Exception {
            // Insert a showtime
            String body = """
                {
                  "showtimeId": 1,
                  "seatNumber": 10,
                  "userId": "not-a-valid-uuid"
                }
            """;
            
            // Try to book with an invalid userId
            mockMvc.perform(post("/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid userId: must be a valid UUID")));
        }
    }
}
