package com.att.tdp.popcorn_palace.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.att.tdp.popcorn_palace.model.movie.Movie;
import com.att.tdp.popcorn_palace.model.booking.Booking;
import com.att.tdp.popcorn_palace.model.showtime.Showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @BeforeEach
    void clearDB() {
        bookingRepository.deleteAll();
        showtimeRepository.deleteAll();
        movieRepository.deleteAll();
    }

    @Test
    @DisplayName("Should detect when seat is already taken (seatNumber unique per showtime)")
    void shouldDetectSeatTaken() {
        // Save a booking
        Long showtimeId = insertValidShowtime();
        bookingRepository.save(new Booking(showtimeId, UUID.randomUUID(), 15));

        // Check existence
        boolean taken = bookingRepository.existsByShowtimeIdAndSeatNumber(showtimeId, 15);
        assertThat(taken).isTrue();
    }

    @Test
    @DisplayName("Should return false if seat is not yet taken")
    void shouldReturnFalseWhenSeatAvailable() {
        Long showtimeId = insertValidShowtime();
        bookingRepository.save(new Booking(showtimeId, UUID.randomUUID(), 15));

        // Check existence
        boolean taken = bookingRepository.existsByShowtimeIdAndSeatNumber(9999L, 99);
        assertThat(taken).isFalse();
    }

    @Test
    @DisplayName("Should save and retrieve booking by ID")
    void shouldSaveAndRetrieve() {
        Long showtimeId = insertValidShowtime();
        bookingRepository.save(new Booking(showtimeId, UUID.randomUUID(), 15));

        // Save a booking
        Booking b = new Booking(showtimeId, UUID.randomUUID(), 10);
        Booking saved = bookingRepository.save(b);

        var found = bookingRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getSeatNumber()).isEqualTo(10);
    }

    // Helper method to insert a valid showtime
    private Long insertValidShowtime() {
        Movie movie = new Movie("Test Movie", "Action", 100, 7.5, 2025);
        movieRepository.save(movie);

        Showtime showtime = new Showtime(
                movie.getId(),
                "Test Theater",
                LocalDateTime.of(2025, 4, 1, 18, 0),
                LocalDateTime.of(2025, 4, 1, 20, 0),
                BigDecimal.valueOf(10.0));
        showtimeRepository.save(showtime);
        return showtime.getId();
    }

}
