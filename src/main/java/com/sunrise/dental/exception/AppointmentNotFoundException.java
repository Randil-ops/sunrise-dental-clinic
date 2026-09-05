package com.sunrise.dental.exception;

/** Thrown when an appointment number does not match any stored record. */
public class AppointmentNotFoundException extends RuntimeException {
    public AppointmentNotFoundException(String message) {
        super(message);
    }
}
