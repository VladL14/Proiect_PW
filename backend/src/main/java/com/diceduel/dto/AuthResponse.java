package com.diceduel.dto;

import com.diceduel.entity.Role;

import java.time.Instant;

/**
 * Result of a successful register or login.
 *
 * @param token     bearer token to be sent as {@code Authorization: Bearer ...}
 * @param expiresAt token expiry instant
 * @param role      effective role of the authenticated account
 * @param account   account profile snapshot
 */
public record AuthResponse(
        String token,
        Instant expiresAt,
        Role role,
        AccountResponse account
) {
}
