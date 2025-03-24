package com.att.tdp.popcorn_palace.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles Bean Validation (@Valid) errors before hitting DB
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String errorMessage = "Validation failed: " + String.join(", ", messages);
        logger.warn("[GlobalExceptionHandler] Validation error - {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    /**
     * Handles DB-level violations (like CHECK constraint failures)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDataIntegrityErrors(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();

        // Optional: parse common cases into friendly responses
        if (message != null && message.contains("movies_rating_check")) {
            message = "Rating must be 10.0 or less (violated DB constraint)";
        }

        logger.error("[GlobalExceptionHandler] DB constraint violation - {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Database constraint violated: " + message);
    }
}
