package com.sunrise.dental.exception;

/** Thrown when appointment input fails a business validation rule. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
