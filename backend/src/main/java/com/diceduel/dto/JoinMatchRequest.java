package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinMatchRequest(
        @NotBlank String playerId
) {
}
