package com.luma.lumacourses.util.exception;

/**
 * Thrown when a unique constraint would be violated (e.g. duplicate
 * email/username).
 * Maps to HTTP 409 Conflict.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
