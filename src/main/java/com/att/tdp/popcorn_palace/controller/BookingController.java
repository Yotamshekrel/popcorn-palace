package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.model.booking.Booking;
import com.att.tdp.popcorn_palace.model.booking.BookingRequest;
import com.att.tdp.popcorn_palace.repository.BookingRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles booking tickets according to the README:
 * POST /bookings -> Book a ticket
 */
@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    /**
     * POST /bookings
     * Request:
     * {
     * "showtimeId": 1,
     * "seatNumber": 15,
     * "userId": "84438967-f68f-4fa0-b620-0f08217e76af"
     * }
     * Response: 200 OK { "bookingId":"uuid-here" }
     */
    @PostMapping
    public ResponseEntity<?> bookTicket(@Valid @RequestBody BookingRequest request) {
        logger.info("Booking ticket for showtime={}, seat={}, user={}",
                request.getShowtimeId(), request.getSeatNumber(), request.getUserId());

        // Check if showtime exists
        if (!showtimeRepository.existsById(request.getShowtimeId())) {
            return ResponseEntity.badRequest().body("No showtime found with id=" + request.getShowtimeId());
        }

        // Check if seat is taken
        boolean seatTaken = bookingRepository.existsByShowtimeIdAndSeatNumber(
                request.getShowtimeId(), request.getSeatNumber());
        if (seatTaken) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Seat " + request.getSeatNumber() + " is already booked for showtime "
                            + request.getShowtimeId());
        }

        // Convert userId from String -> UUID
        UUID userUuid;
        try {
            userUuid = UUID.fromString(request.getUserId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid userId: must be a valid UUID");
        }

        // Create & save booking
        Booking booking = new Booking(request.getShowtimeId(), userUuid, request.getSeatNumber());
        booking = bookingRepository.save(booking);

        // Return booking ID
        logger.info("Booking confirmed! Your booking ID is: {}", booking.getId());
        String msg = "Booking confirmed! Your booking ID is: " + booking.getId();
        return ResponseEntity.ok(msg);
    }
}
