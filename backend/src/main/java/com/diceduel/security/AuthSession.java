package com.diceduel.security;

import com.diceduel.entity.Role;

import java.time.Instant;

/**
 * Immutable description of an authenticated session resolved from a bearer token.
 *
 * @param token      opaque bearer token value
 * @param playerId   identifier of the authenticated player/account
 * @param username   account username (for logging and admin views)
 * @param role       effective access-control role
 * @param issuedAt   moment the token was issued
 * @param expiresAt  moment the token stops being valid
 */
public record AuthSession(
        String token,
        String playerId,
        String username,
        Role role,
        Instant issuedAt,
        Instant expiresAt
) {

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean hasRole(Role required) {
        return role == required;
    }
}
