package com.diceduel.security;

import com.diceduel.entity.Role;

import java.util.Optional;

/**
 * Request-scoped holder for the authenticated session.
 *
 * <p>The {@link AuthInterceptor} populates this thread-local during
 * {@code preHandle} and clears it in {@code afterCompletion}. Controllers and
 * services read the current principal through the static helpers, which keeps
 * the ACL checks close to the business logic where ownership rules live.
 */
public final class CurrentUser {

    private static final ThreadLocal<AuthSession> CONTEXT = new ThreadLocal<>();

    private CurrentUser() {
    }

    public static void set(AuthSession session) {
        CONTEXT.set(session);
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static Optional<AuthSession> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    public static boolean isAuthenticated() {
        return CONTEXT.get() != null;
    }

    public static boolean hasRole(Role role) {
        AuthSession session = CONTEXT.get();
        return session != null && session.role() == role;
    }

    public static boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    /**
     * Returns the authenticated player id, or {@code null} when anonymous.
     */
    public static String playerId() {
        AuthSession session = CONTEXT.get();
        return session == null ? null : session.playerId();
    }
}
