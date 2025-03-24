package com.att.tdp.popcorn_palace.model.booking;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a booking record in the DB.
 */
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Column(name = "booking_time", nullable = false, insertable = false, updatable = false)
    private LocalDateTime bookingTime;

    // Constructors
    public Booking() {
        // Default constructor for JPA
    }

    public Booking(Long showtimeId, UUID userId, int seatNumber) {
        this.showtimeId = showtimeId;
        this.userId = userId;
        this.seatNumber = seatNumber;
    }

    // Getters & setters
    public UUID getId() {return id;}

    public Long getShowtimeId() {return showtimeId;}
    public void setShowtimeId(Long showtimeId) {this.showtimeId = showtimeId;}

    public UUID getUserId() {return userId;}
    public void setUserId(UUID userId) {this.userId = userId;}

    public int getSeatNumber() {return seatNumber;}
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber;}

    public LocalDateTime getBookingTime() {return bookingTime;}
}
