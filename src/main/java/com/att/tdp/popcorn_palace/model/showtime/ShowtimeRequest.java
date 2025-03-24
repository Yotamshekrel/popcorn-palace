package com.att.tdp.popcorn_palace.model.showtime;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating/updating a showtime.
 */
public class ShowtimeRequest {

    @NotNull(message = "movieId is required")
    private Long movieId;

    @NotBlank(message = "theater must not be blank")
    @Size(max = 100, message = "theater must be at most 100 characters")
    private String theater;

    @NotNull(message = "startTime is required")
    private LocalDateTime startTime;

    @NotNull(message = "endTime is required")
    private LocalDateTime endTime;

    @DecimalMin(value = "0.0", message = "price must be >= 0.0")
    private BigDecimal price;

    // Constructors
    public ShowtimeRequest() {
    }

    public ShowtimeRequest(Long movieId, String theater, LocalDateTime startTime,
            LocalDateTime endTime, BigDecimal price) {
        this.movieId = movieId;
        this.theater = theater;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
    }

    // Getters / Setters
    public Long getMovieId() {return movieId;}
    public void setMovieId(Long movieId) {this.movieId = movieId;}

    public String getTheater() {return theater;}
    public void setTheater(String theater) {this.theater = theater;}

    public LocalDateTime getStartTime() {return startTime;}
    public void setStartTime(LocalDateTime startTime) {this.startTime = startTime;}

    public LocalDateTime getEndTime() {return endTime;}
    public void setEndTime(LocalDateTime endTime) {this.endTime = endTime;}

    public BigDecimal getPrice() {return price;}
    public void setPrice(BigDecimal price) {this.price = price;}
}
