package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // If you have a special test profile
@Transactional
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MovieRepository movieRepository;

    @BeforeEach
    void setUp() {
        // Clear DB before each test. 
        // Because we're @Transactional + each test is rolled back, 
        // we typically start fresh. 
        movieRepository.deleteAll();
    }

    @Nested
    @DisplayName("GET /movies/all")
    class GetAllMovies {

        @Test
        @DisplayName("Should return 200 and empty list when no movies in DB")
        void shouldReturnEmptyListIfNoMovies() throws Exception {
            mockMvc.perform(get("/movies/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().json("[]"));
        }

        @Test
        @DisplayName("Should return 200 and list of movies if present")
        void shouldReturnListOfMovies() throws Exception {
            // Insert a sample movie directly into the DB
            movieRepository.save(new Movie("TestTitle", "Action", 120, 8.0, 2000));

            mockMvc.perform(get("/movies/all"))
                    .andExpect(status().isOk())
                    // Since the response is a list, let's just check it's not empty
                    .andExpect(jsonPath("$[0].title").value("TestTitle"))
                    .andExpect(jsonPath("$[0].genre").value("Action"));
        }
    }

    @Nested
    @DisplayName("POST /movies")
    class CreateMovie {

        @Test
        @DisplayName("Should create a valid movie and return 200")
        @Rollback
        void shouldCreateValidMovie() throws Exception {
            String body = """
                {
                  "title": "NewMovie",
                  "genre": "Action",
                  "duration": 100,
                  "rating": 7.5,
                  "releaseYear": 2005
                }
            """;

            mockMvc.perform(post("/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Successfully created the movie:")));
        }

        @Test
        @DisplayName("Should return 409 if title already exists")
        @Rollback
        void shouldReturnConflictWhenTitleExists() throws Exception {
            movieRepository.save(new Movie("DupTitle", "SciFi", 120, 8.0, 2010));

            String body = """
                {
                  "title": "DupTitle",
                  "genre": "SciFi",
                  "duration": 120,
                  "rating": 8.0,
                  "releaseYear": 2010
                }
            """;

            mockMvc.perform(post("/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Another movie already has the title")));
        }

        @Test
        @DisplayName("Should return 400 if invalid data (rating = 15.0)")
        @Rollback
        void shouldReturnBadRequestForInvalidRating() throws Exception {
            String body = """
                {
                  "title": "BadRating",
                  "genre": "Action",
                  "duration": 120,
                  "rating": 15.0,
                  "releaseYear": 2000
                }
            """;

            mockMvc.perform(post("/movies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /movies/update/{movieTitle}")
    class UpdateMovie {

        @Test
        @DisplayName("Should update an existing movie and return 200")
        @Rollback
        void shouldUpdateExistingMovie() throws Exception {
            movieRepository.save(new Movie("OldTitle", "Action", 100, 7.0, 2001));

            String body = """
                {
                  "title": "NewTitle",
                  "genre": "Adventure",
                  "duration": 130,
                  "rating": 8.0,
                  "releaseYear": 2020
                }
            """;

            mockMvc.perform(post("/movies/update/OldTitle")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("was updated successfully")));
        }

        @Test
        @DisplayName("Should return 404 if the original title was not found")
        @Rollback
        void shouldReturnNotFoundIfTitleMissing() throws Exception {
            String body = """
                {
                  "title": "AnyTitle",
                  "genre": "Comedy",
                  "duration": 90,
                  "rating": 6.0,
                  "releaseYear": 2018
                }
            """;

            mockMvc.perform(post("/movies/update/NonExistent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 if renaming to another existing title")
        @Rollback
        void shouldReturnConflictIfRenameToExisting() throws Exception {
            movieRepository.save(new Movie("MovieA", "Action", 120, 8.0, 2010));
            movieRepository.save(new Movie("MovieB", "Drama", 100, 7.0, 2011));

            String body = """
                {
                  "title": "MovieB",
                  "genre": "Comedy",
                  "duration": 90,
                  "rating": 6.0,
                  "releaseYear": 2018
                }
            """;

            mockMvc.perform(post("/movies/update/MovieA")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("title 'MovieB' is already used")));
        }
    }

    @Nested
    @DisplayName("DELETE /movies/{movieTitle}")
    class DeleteMovie {

        @Test
        @DisplayName("Should delete existing movie and return 200")
        @Rollback
        void shouldDeleteExistingMovie() throws Exception {
            movieRepository.save(new Movie("ToDelete", "Thriller", 100, 7.5, 2015));

            mockMvc.perform(delete("/movies/ToDelete"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("was removed successfully")));
        }

        @Test
        @DisplayName("Should return 404 if movie does not exist")
        @Rollback
        void shouldReturnNotFoundForNonexistent() throws Exception {
            mockMvc.perform(delete("/movies/DoesNotExist"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("No movie found with title")));
        }
    }
}
