package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;

public record SetAvatarStyleRequest(
        @NotBlank String style
) {
}