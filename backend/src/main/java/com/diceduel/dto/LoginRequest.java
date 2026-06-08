package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login payload for {@code POST /api/auth/login}.
 *
 * @param usernameOrEmail username or email used as the login identifier
 * @param password        clear-text password to verify
 */
public record LoginRequest(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {
}
