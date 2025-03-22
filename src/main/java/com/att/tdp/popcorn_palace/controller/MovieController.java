package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.model.Movie;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/movies")
public class MovieController {

    @Autowired
    private MovieRepository movieRepository;

    /**
     * GET /movies/all
     * 
     * Fetch all movies.
     * Returns:
     *  200 OK + List of movies in JSON
     */
    @GetMapping("/all")
    public ResponseEntity<List<Movie>> getAllMovies() {
        List<Movie> movies = movieRepository.findAll();
        return ResponseEntity.ok(movies);
    }

    /**
     * POST /movies
     * 
     * Add a new movie.
     * Expected JSON Body:
     *   {
     *     "title": "...",
     *     "genre": "...",
     *     "duration": ...,
     *     "rating": ...,
     *     "releaseYear": ...
     *   }
     * Returns:
     *  200 OK + the created movie JSON
     *  409 Conflict if a movie with the same title exists
     *  400 Bad Request if the request body fails validation
     */
    @PostMapping
    public ResponseEntity<?> addMovie(@Valid @RequestBody Movie movie) {
        // Check if a movie with the same title already exists
        if (movieRepository.existsByTitle(movie.getTitle())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("A movie with this title already exists.");
        }

        // Save the new movie
        Movie savedMovie = movieRepository.save(movie);
        return ResponseEntity.ok(savedMovie);
    }

    /**
     * POST /movies/update/{movieTitle}
     * 
     * Update an existing movie by its original title.
     * Expected JSON Body:
     *   {
     *     "title": "...",
     *     "genre": "...",
     *     "duration": ...,
     *     "rating": ...,
     *     "releaseYear": ...
     *   }
     * PathVariable: {movieTitle} is the original title to look up the existing record.
     * 
     * Returns:
     *  200 OK + the updated movie JSON
     *  404 Not Found if no movie with the specified original title exists
     *  409 Conflict if the new title is already taken by another movie
     *  400 Bad Request if the request body fails validation
     */
    @PostMapping("/update/{movieTitle}")
    public ResponseEntity<?> updateMovieByTitle(
            @PathVariable String movieTitle,
            @Valid @RequestBody Movie updatedData
    ) {
        return movieRepository.findByTitle(movieTitle).map(existingMovie -> {
            // If user tries to rename the movie to a new title that's already taken
            if (!movieTitle.equals(updatedData.getTitle()) &&
                    movieRepository.existsByTitle(updatedData.getTitle())) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("A movie with the new title already exists.");
            }

            // Update fields
            existingMovie.setTitle(updatedData.getTitle());
            existingMovie.setGenre(updatedData.getGenre());
            existingMovie.setDuration(updatedData.getDuration());
            existingMovie.setRating(updatedData.getRating());
            existingMovie.setReleaseYear(updatedData.getReleaseYear());

            // Save updated movie
            Movie saved = movieRepository.save(existingMovie);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /movies/{movieTitle}
     * 
     * Delete a movie by its title.
     * Returns:
     *  200 OK + a success message
     *  404 Not Found if no movie with the specified title exists
     */
    @DeleteMapping("/{movieTitle}")
public ResponseEntity<?> deleteMovieByTitle(@PathVariable String movieTitle) {



    boolean exists = movieRepository.existsByTitle(movieTitle);

    if (!exists) {
        System.out.println("DEBUG: returning 404 because we can't find a match in DB.");
        return ResponseEntity.notFound().build();
    }

    try {
        movieRepository.deleteByTitle(movieTitle);
        return ResponseEntity.ok("Movie deleted successfully.");
    } catch (Exception e) {
        // 5) Print out the actual exception message
        e.printStackTrace();
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Error deleting movie: " + e.getMessage());
    }
}
    
}
