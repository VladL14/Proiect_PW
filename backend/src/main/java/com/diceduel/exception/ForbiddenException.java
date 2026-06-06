package com.diceduel.exception;

/**
 * Raised when an authenticated principal lacks the role or ownership required
 * for the requested operation. Mapped to HTTP 403.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
