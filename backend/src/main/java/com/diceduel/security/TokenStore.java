package com.diceduel.security;

import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.Role;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry of issued bearer tokens.
 *
 * <p>A dedicated session store keeps the authentication concern isolated from
 * the persistence layer and lets the application revoke a token on logout
 * without touching the database. For a production deployment this component
 * would be backed by a signed JWT or a shared cache, but the contract used by
 * the rest of the application stays identical.
 */
@Component
public class TokenStore {

    private static final Duration TOKEN_TTL = Duration.ofHours(12);

    private final ConcurrentHashMap<String, AuthSession> sessions = new ConcurrentHashMap<>();

    /**
     * Issues a new token for an authenticated player.
     *
     * @param player authenticated player entity
     * @return the created session
     */
    public AuthSession issue(PlayerEntity player) {
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();
        Role role = player.getRole() == null ? Role.USER : player.getRole();
        AuthSession session = new AuthSession(
                token,
                player.getId(),
                player.getUsername() != null ? player.getUsername() : player.getName(),
                role,
                now,
                now.plus(TOKEN_TTL)
        );
        sessions.put(token, session);
        return session;
    }

    /**
     * Resolves a token to a non-expired session.
     *
     * @param token bearer token value
     * @return the matching active session, if any
     */
    public Optional<AuthSession> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        AuthSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }
        if (session.isExpired()) {
            sessions.remove(token);
            return Optional.empty();
        }
        return Optional.of(session);
    }

    /**
     * Revokes a token so it can no longer be used (logout).
     *
     * @param token bearer token value
     */
    public void revoke(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }
}
