package dev.shrkptv.authservice.exception;

public class FailedRegistrationException extends RuntimeException {
    public FailedRegistrationException() {
        super("Failed to create user profile. Registration canceled.");
    }
}
