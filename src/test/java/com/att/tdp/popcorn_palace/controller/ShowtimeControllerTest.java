package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.model.showtime.Showtime;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
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

        @Autowired
        MovieRepository movieRepository;

        @BeforeEach
        void setUp() {
                showtimeRepository.deleteAll();
                movieRepository.deleteAll();
        }

        @Nested
        @DisplayName("POST /showtimes")
        class CreateShowtime {

                @Test
                @DisplayName("Should create a valid showtime (200 OK)")
                @Rollback
                void shouldCreateValidShowtime() throws Exception {
                        // Create a movie
                        Long movieId = insertTestMovie();

                        String body = """
                                            {
                                              "movieId": %d,
                                              "theater": "Cinema Hall",
                                              "startTime": "2025-03-25T10:00:00",
                                              "endTime": "2025-03-25T12:00:00",
                                              "price": 10.0
                                            }
                                        """.formatted(movieId);

                        // Create a showtime
                        mockMvc.perform(post("/showtimes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isOk())
                                        .andExpect(content().string(org.hamcrest.Matchers
                                                        .containsString("Successfully created showtime")));
                }

                @Test
                @DisplayName("Should return 400 if endTime <= startTime")
                @Rollback
                void shouldReturnBadRequestInvalidTimes() throws Exception {
                        // Create a movie
                        Long movieId = insertTestMovie();
                        String body = """
                                            {
                                              "movieId": %d,
                                              "theater": "Cinema Hall",
                                              "startTime": "2025-03-25T12:00:00",
                                              "endTime": "2025-03-25T10:00:00",
                                              "price": 10.0
                                            }
                                        """.formatted(movieId);

                        // Attempt to create a showtime with invalid times
                        mockMvc.perform(post("/showtimes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(
                                                        content().string(org.hamcrest.Matchers.containsString(
                                                                        "endTime must be after startTime")));
                }

                @Test
                @DisplayName("Should return 409 if overlapping showtime in same theater")
                @Rollback
                void shouldReturnConflictForOverlap() throws Exception {
                        // Create a movie
                        Long movieId = insertTestMovie();

                        Showtime existing = new Showtime(movieId, "Overlap Theater",
                                        LocalDateTime.of(2025, 3, 25, 10, 0),
                                        LocalDateTime.of(2025, 3, 25, 12, 0),
                                        BigDecimal.valueOf(10.0));
                        showtimeRepository.save(existing);

                        // Attempt to create a showtime that overlaps with the existing one
                        String body = """
                                            {
                                              "movieId": %d,
                                              "theater": "Overlap Theater",
                                              "startTime": "2025-03-25T11:30:00",
                                              "endTime": "2025-03-25T13:30:00",
                                              "price": 9.0
                                            }
                                        """.formatted(movieId);

                        // Expect a 409 Conflict
                        mockMvc.perform(post("/showtimes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isConflict())
                                        .andExpect(content().string(org.hamcrest.Matchers
                                                        .containsString("Another showtime overlaps")));
                }

                @Test
                @DisplayName("Should return 400 if movieId doesn't exist in DB")
                @Rollback
                void shouldReturnBadRequestIfMovieMissing() throws Exception {
                        // Create a movie
                        Long movieId = insertTestMovie();

                        String body = """
                                            {
                                              "movieId": %d,
                                              "theater": "Cinema Hall",
                                              "startTime": "2025-03-25T10:00:00",
                                              "endTime": "2025-03-25T12:00:00",
                                              "price": 10.0
                                            }
                                        """.formatted(movieId + 1);

                        // Attempt to create a showtime with a non-existent movieId
                        mockMvc.perform(post("/showtimes")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isBadRequest())
                                        .andExpect(content().string(org.hamcrest.Matchers
                                                        .containsString("No movie found with id=")));
                }
        }

        @Nested
        @DisplayName("GET /showtimes/{id}")
        class GetShowtime {
                @Test
                @DisplayName("Should return 200 and showtime if found")
                @Rollback
                void shouldReturnShowtime() throws Exception {
                        // Create a movie
                        Long movieId = insertTestMovie();

                        Showtime saved = showtimeRepository.save(
                                        new Showtime(movieId, "Test Theater",
                                                        LocalDateTime.of(2025, 3, 25, 10, 0),
                                                        LocalDateTime.of(2025, 3, 25, 12, 0),
                                                        BigDecimal.valueOf(10.0)));

                        // Fetch the showtime
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
                        // Create a movie
                        Long movieId = insertTestMovie();

                        Showtime existing = showtimeRepository.save(
                                        new Showtime(movieId, "Update Theater",
                                                        LocalDateTime.of(2025, 3, 25, 10, 0),
                                                        LocalDateTime.of(2025, 3, 25, 12, 0),
                                                        BigDecimal.valueOf(10.0)));

                        String body = """
                                        {
                                          "movieId": %d,
                                          "theater": "Update Theater",
                                          "startTime": "2025-03-25T10:00:00",
                                          "endTime": "2025-03-25T12:00:00",
                                          "price": 10.0
                                        }
                                    """.formatted(movieId);

                        // Update the showtime
                        mockMvc.perform(post("/showtimes/update/" + existing.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isOk())
                                        .andExpect(content().string(
                                                        org.hamcrest.Matchers.containsString("updated successfully")));
                }

                @Test
                @DisplayName("Should return 404 if showtime not found")
                @Rollback
                void shouldReturnNotFoundWhenMissing() throws Exception {
                        Long movieId = insertTestMovie();
                        String body = """
                                        {
                                          "movieId": %d,
                                          "theater": "Update Theater",
                                          "startTime": "2025-03-25T10:00:00",
                                          "endTime": "2025-03-25T12:00:00",
                                          "price": 10.0
                                        }
                                    """.formatted(movieId);

                        // Attempt to update a non-existent showtime
                        mockMvc.perform(post("/showtimes/update/999999")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isNotFound());
                }

                @Test
                @DisplayName("Should return 409 if overlapping on update")
                @Rollback
                void shouldReturnConflictIfOverlapOnUpdate() throws Exception {
                        Long movieId = insertTestMovie();
                        // Show 1
                        showtimeRepository.save(
                                        new Showtime(movieId, "Overlap Theater",
                                                        LocalDateTime.of(2025, 3, 25, 10, 0),
                                                        LocalDateTime.of(2025, 3, 25, 12, 0),
                                                        BigDecimal.valueOf(10.0)));

                        // Show 2
                        Showtime s2 = showtimeRepository.save(
                                        new Showtime(movieId, "Overlap Theater",
                                                        LocalDateTime.of(2025, 3, 25, 13, 0),
                                                        LocalDateTime.of(2025, 3, 25, 14, 0),
                                                        BigDecimal.valueOf(8.0)));

                        // Attempt to update showtime 2 so it now overlaps with showtime 1
                        String body = """
                                        {
                                          "movieId": %d,
                                          "theater": "Overlap Theater",
                                          "startTime": "2025-03-25T11:30:00",
                                          "endTime": "2025-03-25T13:30:00",
                                          "price": 9.0
                                        }
                                    """.formatted(movieId);

                        mockMvc.perform(post("/showtimes/update/" + s2.getId())
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(body))
                                        .andExpect(status().isConflict())
                                        .andExpect(content().string(
                                                        org.hamcrest.Matchers.containsString("Overlapping showtime")));
                }
        }

        @Nested
        @DisplayName("DELETE /showtimes/{id}")
        class DeleteShowtime {
                @Test
                @DisplayName("Should delete existing showtime (200 OK)")
                @Rollback
                void shouldDeleteExisting() throws Exception {
                        // Create a movie
                        Long movieId = insertTestMovie();

                        Showtime s = showtimeRepository.save(
                                        new Showtime(movieId, "Delete Theater",
                                                        LocalDateTime.of(2025, 3, 25, 10, 0),
                                                        LocalDateTime.of(2025, 3, 25, 12, 0),
                                                        BigDecimal.valueOf(10.0)));

                        // Delete the showtime
                        mockMvc.perform(delete("/showtimes/" + s.getId()))
                                        .andExpect(status().isOk())
                                        .andExpect(content().string(org.hamcrest.Matchers
                                                        .containsString("was deleted successfully")));
                }

                @Test
                @DisplayName("Should return 404 if showtime not found")
                @Rollback
                void shouldReturnNotFound() throws Exception {
                        // Attempt to delete a non-existent showtime
                        mockMvc.perform(delete("/showtimes/999999"))
                                        .andExpect(status().isNotFound());
                }
        }

        // Helper method to insert a test movie
        private Long insertTestMovie() {
                Movie movie = new Movie("Test Movie", "Action", 120, 7.5, 2024);
                movieRepository.save(movie);
                return movie.getId();
        }

}
