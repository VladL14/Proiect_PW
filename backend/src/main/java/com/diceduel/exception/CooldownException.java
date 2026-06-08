package com.diceduel.exception;

/**
 * Raised when an action is rejected because the caller is still inside a
 * rate-limiting cooldown window (for example emote spam protection).
 * Mapped to HTTP 429 Too Many Requests.
 */
public class CooldownException extends RuntimeException {

    public CooldownException(String message) {
        super(message);
    }
}
