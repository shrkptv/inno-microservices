package dev.shrkptv.userservice.exception;

public class UserNotFoundByEmailException extends RuntimeException {
    public UserNotFoundByEmailException(String email) {
        super("User with email '" + email + "' not found");
    }
}
