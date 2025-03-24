package com.att.tdp.popcorn_palace.model.movie;

import jakarta.validation.constraints.*;

/*
 * This class represents the DTO request body for creating a new movie.
 */
public class MovieRequest {

    @NotBlank(message = "Title must not be empty")
    private String title;

    @Pattern(regexp = "^[A-Za-z ]+$", message = "Genre must contain only English letters and spaces")
    private String genre;

    @Min(value = 1, message = "Duration must be greater than 0")
    private Integer duration;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
    private Double rating;

    @Min(value = 1800, message = "Release year must not be before 1800")
    @Max(value = 2025, message = "Release year must not exceed the current year (2025)")
    private Integer releaseYear;

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}

    public String getGenre() {return genre;}
    public void setGenre(String genre) {this.genre = genre;}

    public Integer getDuration() {return duration;}
    public void setDuration(Integer duration) {this.duration = duration;}

    public Double getRating() {return rating;}
    public void setRating(Double rating) {this.rating = rating;}

    public Integer getReleaseYear() {return releaseYear;}
    public void setReleaseYear(Integer releaseYear) {this.releaseYear = releaseYear;}
}
