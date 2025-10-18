package dev.shrkptv.authservice.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Token is invalid");
    }
}
