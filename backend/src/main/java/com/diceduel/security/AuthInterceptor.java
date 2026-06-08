package com.diceduel.security;

import com.diceduel.entity.Role;
import com.diceduel.exception.ForbiddenException;
import com.diceduel.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Optional;

/**
 * Authenticates every API request from the {@code Authorization: Bearer ...}
 * header and enforces {@link RequireRole} declarations.
 *
 * <p>Authentication is best-effort for unannotated handlers (legacy gameplay
 * endpoints stay reachable), but the resolved {@link AuthSession} is always
 * published through {@link CurrentUser} so service-level ownership checks can
 * use it. Handlers annotated with {@link RequireRole} are hard-gated: a missing
 * token yields {@code 401} and an insufficient role yields {@code 403}.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenStore tokenStore;

    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {
        AuthSession session = resolveSession(request).orElse(null);
        CurrentUser.set(session);

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = findRequireRole(handlerMethod);
        if (requireRole == null) {
            return true;
        }
        if (session == null) {
            throw new UnauthorizedException("Authentication is required for this resource");
        }
        boolean allowed = Arrays.stream(requireRole.value()).anyMatch(role -> role == session.role());
        if (!allowed) {
            throw new ForbiddenException("Role " + session.role() + " is not allowed to access this resource");
        }
        return true;
    }

    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex
    ) {
        CurrentUser.clear();
    }

    private Optional<AuthSession> resolveSession(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        return tokenStore.resolve(header.substring(BEARER_PREFIX.length()).trim());
    }

    private RequireRole findRequireRole(HandlerMethod handlerMethod) {
        RequireRole methodLevel = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (methodLevel != null) {
            return methodLevel;
        }
        return handlerMethod.getBeanType().getAnnotation(RequireRole.class);
    }

    /**
     * Convenience used by the WebSocket layer, which is outside the MVC chain.
     */
    public static Role roleOrGuest() {
        return CurrentUser.get().map(AuthSession::role).orElse(Role.GUEST);
    }
}
