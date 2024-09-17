package com.message.messi.exception;

public class InvalidInputException extends RuntimeException {

    private final String message;

    public InvalidInputException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
