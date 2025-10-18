package dev.shrkptv.authservice.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String login) {
        super("Username '" + login + "' already exists!");
    }
}
