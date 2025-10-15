package dev.shrkptv.orderservice.controller;

import dev.shrkptv.orderservice.exception.OrderNotFoundByIdException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<Object> handleOrderNotFoundById(OrderNotFoundByIdException ex)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("Error", ex.getMessage()));
    }
}
