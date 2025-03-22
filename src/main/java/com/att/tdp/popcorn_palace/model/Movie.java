package com.att.tdp.popcorn_palace.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "movies", uniqueConstraints = {
    @UniqueConstraint(columnNames = "title") // Enforce unique titles
})
public class Movie {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Internal primary key

    @NotBlank(message = "Title must not be empty")
    @Column(nullable = false)
    private String title;

    /**
     * English letters (upper or lower) and spaces only.
     */
    @Pattern(
        regexp = "^[A-Za-z ]+$",
        message = "Genre must contain only English letters and spaces"
    )
    private String genre;

    /**
     * Duration in minutes. Must be positive.
     */
    @Min(value = 1, message = "Duration must be greater than 0")
    private Integer duration;

    /**
     * Rating must be double between 0 to 10.
     */
    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
    private Double rating;

    /**
     * Must be a valid year.
     */
    @Min(value = 1800, message = "Release year must not be before 1800")
    @Max(value = 2025, message = "Release year must not exceed the current year (2025)")
    private Integer releaseYear;

   
    public Movie(String title, String genre, Integer duration, Double rating, Integer releaseYear) {
        this.title = title;
        this.genre = genre;
        this.duration = duration;
        this.rating = rating;
        this.releaseYear = releaseYear;
    }

     // Getters and Setters
     public Long getId() { return id; }
     public void setId(Long id) { this.id = id; }
 
     public String getTitle() { return title; }
     public void setTitle(String title) { this.title = title; }
 
     public String getGenre() { return genre; }
     public void setGenre(String genre) { this.genre = genre; }
 
     public Integer getDuration() { return duration; }
     public void setDuration(Integer duration) { this.duration = duration; }
 
     public Double getRating() { return rating; }
     public void setRating(Double rating) { this.rating = rating; }
 
     public Integer getReleaseYear() { return releaseYear; }
     public void setReleaseYear(Integer releaseYear) { this.releaseYear = releaseYear; }
 }

