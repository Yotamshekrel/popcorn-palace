package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.model.movie.Movie;
import com.att.tdp.popcorn_palace.model.movie.MovieRequest;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * Controller for handling all movie-related API endpoints.
 */
@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    /**
     * Endpoint: GET /movies/all
     * 
     * Retrieve a list of all movies.
     * 
     * @return 200 OK with the list of all movies in JSON
     */
    @GetMapping("/all")
    public ResponseEntity<List<Movie>> getAllMovies() {
        System.out.println("[MovieController] INFO - Request to fetch all movies.");
        List<Movie> movies = movieRepository.findAll();

        System.out.println("[MovieController] INFO - Returning " + movies.size() + " movies.");

        return ResponseEntity.ok(movies);
    }

    /**
     * Endpoint: POST /movies
     * 
     * Create a new movie entry.
     * 
     * Expects a JSON body with title, genre, duration, rating, releaseYear.
     * 
     * @param movie The movie details sent by the client.
     * @return 200 OK + newly created movie JSON,
     *         or 409 Conflict if a movie with the same title exists,
     *         or 400 Bad Request if validation fails.
     */
    @PostMapping
    public ResponseEntity<String> addMovie(@Valid @RequestBody MovieRequest movieDto) {
        if (movieRepository.existsByTitle(movieDto.getTitle())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Another movie already has the title '" + movieDto.getTitle()
                            + "'. Please pick a unique title.");
        }

        Movie savedMovie = movieRepository.save(mapToEntity(movieDto));

        String successMsg = "Successfully created the movie: '" + savedMovie.getTitle() + "with id: "+savedMovie.getId()+ "'.";
        return ResponseEntity.ok(successMsg);

    }

    /**
     * Endpoint: POST /movies/update/{movieTitle}
     * 
     * Update an existing movie by looking it up via its original title.
     * 
     * @param movieTitle  The original title used to find the existing record.
     * @param updatedData The new movie details from the client.
     * @return 200 OK + updated movie info, or 404 if not found,
     *         or 409 Conflict if the new title is taken,
     *         or 400 if the request body fails validation.
     */
    @PostMapping("/update/{movieTitle}")
    public ResponseEntity<String> updateMovieByTitle(
            @PathVariable String movieTitle,
            @Valid @RequestBody MovieRequest updatedData) {
        return movieRepository.findByTitle(movieTitle).map(existingMovie -> {
            boolean wantsToRename = !movieTitle.equals(updatedData.getTitle());
            boolean newTitleTaken = movieRepository.existsByTitle(updatedData.getTitle());

            if (wantsToRename && newTitleTaken) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Sorry, the title '" + updatedData.getTitle() + "' is already used by another movie.");
            }

            existingMovie.setTitle(updatedData.getTitle());
            existingMovie.setGenre(updatedData.getGenre());
            existingMovie.setDuration(updatedData.getDuration());
            existingMovie.setRating(updatedData.getRating());
            existingMovie.setReleaseYear(updatedData.getReleaseYear());

            Movie saved = movieRepository.save(existingMovie);

            String successMsg = "Movie '" + movieTitle + "' was updated successfully. New title is '"
                    + saved.getTitle() + "'.";
            return ResponseEntity.ok(successMsg);

        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Movie with title '" + movieTitle + "' was not found. Update failed."));

    }

    /**
     * Endpoint: DELETE /movies/{movieTitle}
     * 
     * Delete a movie by its title.
     * 
     * @param movieTitle The unique title of the movie to be removed.
     * @return 200 OK on success, 404 if the movie doesn't exist,
     *         500 if there's a server or DB error.
     */
    @DeleteMapping("/{movieTitle}")
    public ResponseEntity<String> deleteMovieByTitle(@PathVariable String movieTitle) {
        System.out.println("[MovieController] INFO - Attempting to delete movie: '" + movieTitle + "'");

        // Check if there's a matching movie
        boolean movieExists = movieRepository.existsByTitle(movieTitle);
        if (!movieExists) {
            System.out.println("[MovieController] WARN - Movie '" + movieTitle + "' does not exist for deletion.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No movie found with title '" + movieTitle + "'. Could not delete.");
        }

        // Try performing the delete
        try {
            movieRepository.deleteByTitle(movieTitle);

            String successMsg = "Movie '" + movieTitle + "' was removed successfully.";
            System.out.println("[MovieController] SUCCESS - " + successMsg);
            return ResponseEntity.ok(successMsg);

        } catch (Exception e) {
            System.out.println("[MovieController] ERROR - Exception while deleting movie: " + e.getMessage());
            e.printStackTrace();

            // Return a user-friendly message
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Something went wrong while deleting '" + movieTitle
                            + "'. Please try again or contact support.");
        }
    }

    private Movie mapToEntity(MovieRequest dto) {
        return new Movie(
                dto.getTitle(),
                dto.getGenre(),
                dto.getDuration(),
                dto.getRating(),
                dto.getReleaseYear());
    }

}
