package dev.shrkptv.userservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class RestControllerExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler
    public ResponseEntity<Object> handleUserNotFoundById(UserNotFoundByIdException ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Error", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleUserNotFoundByEmail(UserNotFoundByEmailException ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Error", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleUserAlreadyExists(UserAlreadyExistsException ex)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("Error", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleCardNotFound(CardNotFoundException ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Error", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Object> handleOtherExceptions(Exception ex) {
        log.error(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("Error", "Internal error: " + ex.getMessage()));
    }
}
