package dev.shrkptv.orderservice.exception;

public class OrderNotFoundByIdException extends RuntimeException {
    public OrderNotFoundByIdException(Long id) {
        super("Order not found with id: " + id);
    }
}
