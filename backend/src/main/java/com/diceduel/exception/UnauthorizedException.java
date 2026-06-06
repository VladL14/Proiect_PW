package com.diceduel.exception;

/**
 * Raised when a request is not authenticated, e.g. missing/invalid/expired token
 * or wrong credentials. Mapped to HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
