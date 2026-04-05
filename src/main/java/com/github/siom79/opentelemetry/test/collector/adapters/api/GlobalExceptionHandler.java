package com.github.siom79.opentelemetry.test.collector.adapters.api;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.siom79.opentelemetry.test.collector.core.model.traces.OpentelemetryTestCollectorException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OpentelemetryTestCollectorException.class)
    public ResponseEntity<ErrorResponse> handleCollectorException(OpentelemetryTestCollectorException ex) {
        String errorId = UUID.randomUUID().toString();
        log.warn("Application error [{}]: {}", errorId, ex.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(errorId, ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        log.error("Unexpected error [{}]", errorId, ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(errorId, "An unexpected error occurred"));
    }

    public record ErrorResponse(String errorId, String message) {
    }
}
