package com.att.tdp.popcorn_palace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.att.tdp.popcorn_palace.model.showtime.Showtime;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    /**
     * Find showtimes in the same theater that overlap the given time range.
     * 
     * We exclude the showtime with id = ignoreId so we don't conflict with itself
     * during an update scenario.
     */
    @Query("""
                SELECT s
                FROM Showtime s
                WHERE s.theater = :theater
                  AND s.id <> :ignoreId
                  AND (:startTime < s.endTime)
                  AND (s.startTime < :endTime)
            """)
    List<Showtime> findOverlappingShowtimes(
            @Param("theater") String theater,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("ignoreId") Long ignoreId);
}
