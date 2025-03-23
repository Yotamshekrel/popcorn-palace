package com.att.tdp.popcorn_palace.controller;

import com.att.tdp.popcorn_palace.model.showtime.Showtime;
import com.att.tdp.popcorn_palace.model.showtime.ShowtimeRequest;
import com.att.tdp.popcorn_palace.repository.MovieRepository;
import com.att.tdp.popcorn_palace.repository.ShowtimeRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/showtimes")
public class ShowtimeController {

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private MovieRepository movieRepository;

    /**
     * Endpoint: POST /showtimes
     * Create a new showtime.
     */
    @PostMapping
    public ResponseEntity<String> createShowtime(@Valid @RequestBody ShowtimeRequest request) {
        System.out
                .println("[ShowtimeController] INFO - Request to create showtime for movieId=" + request.getMovieId());

        // 1) Check if the movie exists
        if (!movieRepository.existsById(request.getMovieId())) {
            System.out.println("[ShowtimeController] WARN - Movie with id=" + request.getMovieId() + " not found");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No movie found with id=" + request.getMovieId());
        }

        // 2) Check if endTime > startTime
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            return ResponseEntity.badRequest().body("endTime must be after startTime");
        }

        // 3) Check overlap with existing showtimes in the same theater
        boolean overlap = !showtimeRepository.findOverlappingShowtimes(
                request.getTheater(),
                request.getStartTime(),
                request.getEndTime(),
                0L // ignoreId=0 for create
        ).isEmpty();

        if (overlap) {
            System.out.println("[ShowtimeController] WARN - Overlapping showtime in theater " + request.getTheater());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Another showtime overlaps in theater '" + request.getTheater() + "'.");
        }

        // 4) Save to DB
        Showtime showtime = mapRequestToEntity(request, null); // no existing ID yet
        Showtime saved = showtimeRepository.save(showtime);

        String msg = "Successfully created showtime with ID=" + saved.getId();
        System.out.println("[ShowtimeController] SUCCESS - " + msg);
        return ResponseEntity.ok(msg);
    }

    /**
     * Endpoint: GET /showtimes
     * Fetch a specific showtime by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getShowtime(@PathVariable Long id) {
        System.out.println("[ShowtimeController] INFO - Fetching showtime id=" + id);

        Optional<Showtime> found = showtimeRepository.findById(id);
        if (found.isEmpty()) {
            String msg = "Showtime with ID " + id + " not found.";
            System.out.println("[ShowtimeController] WARN - " + msg);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
        }
        

        return ResponseEntity.ok(found.get());
    }

    /**
     * Endpoint: POST /showtimes/update/{id}
     * Update an existing showtime by ID
     */
    @PostMapping("/update/{id}")
    public ResponseEntity<String> updateShowtime(@PathVariable Long id, @Valid @RequestBody ShowtimeRequest request) {
        System.out.println("[ShowtimeController] INFO - Request to update showtime id=" + id);

        // 1) Find existing
        Optional<Showtime> existingOpt = showtimeRepository.findById(id);
        if (existingOpt.isEmpty()) {
            String msg = "Showtime with ID " + id + " not found. Update aborted.";
            System.out.println("[ShowtimeController] WARN - " + msg);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
        }

        // 2) Validate movie existence
        if (!movieRepository.existsById(request.getMovieId())) {
            return ResponseEntity.badRequest().body("No movie found with id=" + request.getMovieId());
        }

        // 3) Validate time range
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            return ResponseEntity.badRequest().body("endTime must be after startTime");
        }

        // 4) Check overlap (exclude itself)
        boolean overlap = !showtimeRepository.findOverlappingShowtimes(
                request.getTheater(),
                request.getStartTime(),
                request.getEndTime(),
                id).isEmpty();

        if (overlap) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Overlapping showtime in theater '" + request.getTheater() + "'.");
        }

        // 5) Update & save
        Showtime existing = existingOpt.get();
        Showtime updatedEntity = mapRequestToEntity(request, existing.getId());

        showtimeRepository.save(updatedEntity);

        String msg = "Showtime id=" + id + " updated successfully.";
        System.out.println("[ShowtimeController] SUCCESS - " + msg);
        return ResponseEntity.ok(msg);
    }

    /**
     * Endpoint: DELETE /showtimes/{id}
     * Delete an existing showtime by ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteShowtime(@PathVariable Long id) {
        System.out.println("[ShowtimeController] INFO - Deleting showtime id=" + id);

        if (!showtimeRepository.existsById(id)) {
            String msg = "Showtime with ID " + id + " not found. Nothing to delete.";
            System.out.println("[ShowtimeController] WARN - " + msg);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(msg);
        }

        // Perform the delete
        showtimeRepository.deleteById(id);
        String msg = "Showtime id=" + id + " was deleted successfully.";
        System.out.println("[ShowtimeController] SUCCESS - " + msg);
        return ResponseEntity.ok(msg);
    }

    /**
     * Helper to map ShowtimeRequest -> Showtime entity.
     * If existingId is null, we create new. If not, we set the ID for update.
     */
    private Showtime mapRequestToEntity(ShowtimeRequest req, Long existingId) {
        Showtime s = new Showtime();
        if (existingId != null) {
            s.setId(existingId);
        }
        s.setMovieId(req.getMovieId());
        s.setTheater(req.getTheater());
        s.setStartTime(req.getStartTime());
        s.setEndTime(req.getEndTime());
        s.setPrice(req.getPrice() == null ? BigDecimal.ZERO : req.getPrice());

        return s;
    }
}
