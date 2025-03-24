package com.att.tdp.popcorn_palace.model.booking;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for booking a ticket.
 */
public class BookingRequest {

    @NotNull(message = "showtimeId is required")
    private Long showtimeId;

    @Min(value = 1, message = "seatNumber must be > 0")
    private int seatNumber;

    @NotBlank(message = "userId is required")
    private String userId;

    // Constructors
    public BookingRequest() {
    }

    public BookingRequest(Long showtimeId, int seatNumber, String userId) {
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.userId = userId;
    }

    // Getters & setters
    public Long getShowtimeId() {return showtimeId;}
    public void setShowtimeId(Long showtimeId) {this.showtimeId = showtimeId;}

    public int getSeatNumber() {return seatNumber;}
    public void setSeatNumber(int seatNumber) {this.seatNumber = seatNumber;}

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}
}
