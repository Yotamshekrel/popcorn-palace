package com.att.tdp.popcorn_palace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.att.tdp.popcorn_palace.model.booking.Booking;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Check if a seat is already booked for a given showtime.
     */
    boolean existsByShowtimeIdAndSeatNumber(Long showtimeId, int seatNumber);
}
