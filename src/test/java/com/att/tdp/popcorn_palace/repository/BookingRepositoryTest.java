package com.att.tdp.popcorn_palace.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.att.tdp.popcorn_palace.model.booking.Booking;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    void clearDB() {
        bookingRepository.deleteAll();
    }

    @Test
    @DisplayName("Should detect when seat is already taken (seatNumber unique per showtime)")
    void shouldDetectSeatTaken() {
        // Save a booking
        bookingRepository.save(new Booking(1L, UUID.randomUUID(), 15));

        // Check existence
        boolean taken = bookingRepository.existsByShowtimeIdAndSeatNumber(1L, 15);
        assertThat(taken).isTrue();
    }

    @Test
    @DisplayName("Should return false if seat is not yet taken")
    void shouldReturnFalseWhenSeatAvailable() {
        // Check existence
        boolean taken = bookingRepository.existsByShowtimeIdAndSeatNumber(1L, 99);
        assertThat(taken).isFalse();
    }

    @Test
    @DisplayName("Should save and retrieve booking by ID")
    void shouldSaveAndRetrieve() {
        // Save a booking
        Booking b = new Booking(2L, UUID.randomUUID(), 10);
        Booking saved = bookingRepository.save(b);

        var found = bookingRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSeatNumber()).isEqualTo(10);
    }
}
