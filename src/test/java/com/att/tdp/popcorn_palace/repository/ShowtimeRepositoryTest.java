package com.att.tdp.popcorn_palace.repository;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import com.att.tdp.popcorn_palace.model.showtime.Showtime;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ShowtimeRepositoryTest {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @BeforeEach
    void clearDB() {
        showtimeRepository.deleteAll();
    }

    @Nested
    @DisplayName("findOverlappingShowtimes()")
    class OverlapTests {

        @Test
        @DisplayName("Should return no overlaps when none exist")
        void shouldReturnNone() {
            // Setup
            showtimeRepository.save(new Showtime(1L, "TheaterA",
                LocalDateTime.of(2025,3,25,10,0),
                LocalDateTime.of(2025,3,25,12,0),
                BigDecimal.valueOf(10.0)));

            // Test a new range that does NOT overlap
            List<Showtime> overlaps = showtimeRepository.findOverlappingShowtimes(
                "TheaterA",
                LocalDateTime.of(2025,3,25,12,0),
                LocalDateTime.of(2025,3,25,13,0),
                0L
            );

            assertThat(overlaps).isEmpty();
        }

        @Test
        @DisplayName("Should detect overlapping times")
        void shouldDetectOverlap() {
            Showtime existing = showtimeRepository.save(new Showtime(1L, "OverlapTheater",
                LocalDateTime.of(2025,3,25,10,0),
                LocalDateTime.of(2025,3,25,12,0),
                BigDecimal.valueOf(10.0)));

            // Query that intersects 10:00 - 12:00
            List<Showtime> overlaps = showtimeRepository.findOverlappingShowtimes(
                "OverlapTheater",
                LocalDateTime.of(2025,3,25,11,30),
                LocalDateTime.of(2025,3,25,12,30),
                0L // ignoreId=0 means new
            );

            assertThat(overlaps).hasSize(1);
            assertThat(overlaps.get(0).getId()).isEqualTo(existing.getId());
        }

        @Test
        @DisplayName("Should exclude itself when ignoring ID on update")
        void shouldExcludeItself() {
            Showtime s = showtimeRepository.save(new Showtime(1L, "SameTheater",
                LocalDateTime.of(2025,3,25,10,0),
                LocalDateTime.of(2025,3,25,12,0),
                BigDecimal.valueOf(10.0)));

            // Overlap query that matches the same showtime
            List<Showtime> overlaps = showtimeRepository.findOverlappingShowtimes(
                "SameTheater",
                LocalDateTime.of(2025,3,25,10,30),
                LocalDateTime.of(2025,3,25,11,0),
                s.getId() // We pass the same ID
            );
            // Because we pass ignoreId = s.getId(), it should NOT be considered an overlap
            assertThat(overlaps).isEmpty();
        }

        @Test
        @DisplayName("Should not overlap if different theater")
        void differentTheater() {
            showtimeRepository.save(new Showtime(1L, "TheaterX",
                LocalDateTime.of(2025,3,25,10,0),
                LocalDateTime.of(2025,3,25,12,0),
                BigDecimal.valueOf(10.0)));

            List<Showtime> overlaps = showtimeRepository.findOverlappingShowtimes(
                "DifferentTheater",
                LocalDateTime.of(2025,3,25,11,0),
                LocalDateTime.of(2025,3,25,12,0),
                0L
            );
            assertThat(overlaps).isEmpty();
        }
    }

    @Test
    @DisplayName("Should save and retrieve a showtime")
    void shouldSaveRetrieve() {
        Showtime s = new Showtime(2L, "BasicTheater",
                LocalDateTime.of(2025,3,30,14,0),
                LocalDateTime.of(2025,3,30,16,0),
                BigDecimal.valueOf(15.0));
        Showtime saved = showtimeRepository.save(s);

        var found = showtimeRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTheater()).isEqualTo("BasicTheater");
    }
}
