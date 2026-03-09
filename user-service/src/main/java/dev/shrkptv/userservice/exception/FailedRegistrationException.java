package dev.shrkptv.userservice.exception;

public class FailedRegistrationException extends RuntimeException {
    public FailedRegistrationException() {
        super("Failed to create user profile. Registration canceled.");
    }
}
