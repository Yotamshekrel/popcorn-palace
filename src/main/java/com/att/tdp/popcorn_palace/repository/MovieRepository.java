package com.att.tdp.popcorn_palace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.att.tdp.popcorn_palace.model.movie.Movie;

import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Find a movie by its title.
     * 
     * @param title The unique title of the movie.
     * @return An Optional containing the found Movie, or empty if no match.
     */
    Optional<Movie> findByTitle(String title);

    /**
     * Delete a movie by its title.
     *
     * @param title The title of the movie to delete.
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Movie m WHERE m.title = :title")
    void deleteByTitle(String title);

    /**
     * Check if a movie with the given title already exists.
     *
     * @param title The title of the movie.
     * @return True if a movie with this title exists; false otherwise.
     */
    boolean existsByTitle(String title);
}
