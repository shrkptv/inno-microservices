package dev.shrkptv.authservice.controller;

import dev.shrkptv.authservice.exception.FailedRegistrationException;
import dev.shrkptv.authservice.exception.InvalidTokenException;
import dev.shrkptv.authservice.exception.UsernameAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<Object> handleUsernameAlreadyExists(UsernameAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("Error", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleInvalidToken(InvalidTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Error", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleFailedRegistration(FailedRegistrationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("Error", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleOtherExceptions(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("Error", "Internal error: " + ex.getMessage()));
    }
}
