package com.att.tdp.popcorn_palace.model.showtime;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing the 'showtimes' table in the DB.
 * 
 * The DB schema enforces:
 *  - price >= 0
 *  - end_time > start_time
 *  - foreign key to movies(id)
 *  - theater up to 100 chars
 */
@Entity
@Table(name = "showtimes")
public class Showtime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id", nullable = false)
    private Long movieId; // References 'movies.id'

    @Column(name = "theater", length = 100, nullable = false)
    private String theater;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "price", nullable = false)
    private BigDecimal price; // e.g. 12.50

    // Constructors
    public Showtime() {
        // Default constructor for JPA
    }

    public Showtime(Long movieId, String theater, LocalDateTime startTime, LocalDateTime endTime, BigDecimal price) {
        this.movieId = movieId;
        this.theater = theater;
        this.startTime = startTime;
        this.endTime = endTime;
        this.price = price;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }

    public String getTheater() { return theater; }
    public void setTheater(String theater) { this.theater = theater; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
