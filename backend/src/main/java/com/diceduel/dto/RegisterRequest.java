package com.diceduel.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Account creation payload for {@code POST /api/auth/register}.
 *
 * @param username    unique login handle
 * @param email       unique contact email
 * @param password    clear-text password (hashed before storage)
 * @param displayName optional in-game display name; defaults to the username
 */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 60) String username,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 100) String password,
        String displayName
) {
}
