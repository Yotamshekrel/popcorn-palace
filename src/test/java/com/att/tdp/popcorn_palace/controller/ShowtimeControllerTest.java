package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.model.showtime.Showtime;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShowtimeRepository showtimeRepository;



    @BeforeEach
    void setUp() {
        // Clear the relevant DB tables
        showtimeRepository.deleteAll();
        // For these tests, ensure at least one valid movie ID exists in the DB:
        // e.g., create a dummy movie with ID=1 if needed, or assume already present
        // since you're using a real Postgres DB, you might do:
        // movieRepository.save(new Movie("TestTitle", "TestGenre", 120, 8.0, 2020));
        // But only if you need a guaranteed movie with id=1 or 2, etc.
    }

    @Nested
    @DisplayName("POST /showtimes")
    class CreateShowtime {

        @Test
        @DisplayName("Should create a valid showtime (200 OK)")
        @Rollback
        void shouldCreateValidShowtime() throws Exception {
            // Make sure a valid movie exists (id=1)
            // Or adapt to a real existing movie ID in your DB
            // e.g. movieRepository.save(...) or assume it's there

            String body = """
                {
                  "movieId": 1,
                  "theater": "Test Theater",
                  "startTime": "2025-03-25T10:00:00",
                  "endTime": "2025-03-25T12:00:00",
                  "price": 12.50
                }
            """;

            mockMvc.perform(post("/showtimes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Successfully created showtime")));
        }

        @Test
        @DisplayName("Should return 400 if endTime <= startTime")
        @Rollback
        void shouldReturnBadRequestInvalidTimes() throws Exception {
            String body = """
                {
                  "movieId": 1,
                  "theater": "Invalid Theater",
                  "startTime": "2025-03-25T12:00:00",
                  "endTime": "2025-03-25T12:00:00",
                  "price": 10.0
                }
            """;

            mockMvc.perform(post("/showtimes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("endTime must be after startTime")));
        }

        @Test
        @DisplayName("Should return 409 if overlapping showtime in same theater")
        @Rollback
        void shouldReturnConflictForOverlap() throws Exception {
            // Insert one showtime
            Showtime existing = new Showtime(1L, "Overlap Theater",
                LocalDateTime.of(2025,3,25,10,0),
                LocalDateTime.of(2025,3,25,12,0),
                BigDecimal.valueOf(10.0));
            showtimeRepository.save(existing);

            // Attempt another that overlaps in same theater
            String body = """
                {
                  "movieId": 1,
                  "theater": "Overlap Theater",
                  "startTime": "2025-03-25T11:30:00",
                  "endTime": "2025-03-25T13:00:00",
                  "price": 10.0
                }
            """;

            mockMvc.perform(post("/showtimes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Another showtime overlaps")));
        }

        @Test
        @DisplayName("Should return 400 if movieId doesn't exist in DB")
        @Rollback
        void shouldReturnBadRequestIfMovieMissing() throws Exception {
            String body = """
                {
                  "movieId": 999999,
                  "theater": "Cinema Hall",
                  "startTime": "2025-03-25T10:00:00",
                  "endTime": "2025-03-25T12:00:00",
                  "price": 10.0
                }
            """;

            mockMvc.perform(post("/showtimes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("No movie found with id=")));
        }
    }

    @Nested
    @DisplayName("GET /showtimes/{id}")
    class GetShowtime {
        @Test
        @DisplayName("Should return 200 and showtime if found")
        @Rollback
        void shouldReturnShowtime() throws Exception {
            Showtime saved = showtimeRepository.save(
                    new Showtime(1L, "Test Theater",
                        LocalDateTime.of(2025,3,25,10,0),
                        LocalDateTime.of(2025,3,25,12,0),
                        BigDecimal.valueOf(10.0))
            );

            mockMvc.perform(get("/showtimes/" + saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.theater").value("Test Theater"));
        }

        @Test
        @DisplayName("Should return 404 if showtime not found")
        void shouldReturnNotFound() throws Exception {
            mockMvc.perform(get("/showtimes/999999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /showtimes/update/{id}")
    class UpdateShowtime {

        @Test
        @DisplayName("Should update existing showtime and return 200")
        @Rollback
        void shouldUpdateExistingShowtime() throws Exception {
            Showtime existing = showtimeRepository.save(
                new Showtime(1L, "Update Theater",
                    LocalDateTime.of(2025,3,25,10,0),
                    LocalDateTime.of(2025,3,25,12,0),
                    BigDecimal.valueOf(10.0))
            );

            String body = """
                {
                  "movieId": 1,
                  "theater": "Updated Theater",
                  "startTime": "2025-03-25T11:00:00",
                  "endTime": "2025-03-25T13:00:00",
                  "price": 15.0
                }
            """;

            mockMvc.perform(post("/showtimes/update/" + existing.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("updated successfully")));
        }

        @Test
        @DisplayName("Should return 404 if showtime not found")
        @Rollback
        void shouldReturnNotFoundWhenMissing() throws Exception {
            String body = """
                {
                  "movieId": 1,
                  "theater": "Doesn't matter",
                  "startTime": "2025-03-25T10:00:00",
                  "endTime": "2025-03-25T12:00:00",
                  "price": 10.0
                }
            """;

            mockMvc.perform(post("/showtimes/update/999999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 if overlapping on update")
        @Rollback
        void shouldReturnConflictIfOverlapOnUpdate() throws Exception {
            // Show 1
            showtimeRepository.save(
                new Showtime(1L, "Overlap Theater",
                    LocalDateTime.of(2025,3,25,10,0),
                    LocalDateTime.of(2025,3,25,12,0),
                    BigDecimal.valueOf(10.0))
            );

            // Show 2
            Showtime s2 = showtimeRepository.save(
                new Showtime(1L, "Overlap Theater",
                    LocalDateTime.of(2025,3,25,13,0),
                    LocalDateTime.of(2025,3,25,14,0),
                    BigDecimal.valueOf(8.0))
            );

            // Attempt to update showtime 2 so it now overlaps with showtime 1
            String body = """
                {
                  "movieId": 1,
                  "theater": "Overlap Theater",
                  "startTime": "2025-03-25T11:30:00",
                  "endTime": "2025-03-25T13:30:00",
                  "price": 9.0
                }
            """;

            mockMvc.perform(post("/showtimes/update/" + s2.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Overlapping showtime")));
        }
    }

    @Nested
    @DisplayName("DELETE /showtimes/{id}")
    class DeleteShowtime {
        @Test
        @DisplayName("Should delete existing showtime (200 OK)")
        @Rollback
        void shouldDeleteExisting() throws Exception {
            Showtime s = showtimeRepository.save(
                new Showtime(1L, "Delete Theater",
                    LocalDateTime.of(2025,3,25,10,0),
                    LocalDateTime.of(2025,3,25,12,0),
                    BigDecimal.valueOf(10.0))
            );

            mockMvc.perform(delete("/showtimes/" + s.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("was deleted successfully")));
        }

        @Test
        @DisplayName("Should return 404 if showtime not found")
        @Rollback
        void shouldReturnNotFound() throws Exception {
            mockMvc.perform(delete("/showtimes/999999"))
                    .andExpect(status().isNotFound());
        }
    }
}
