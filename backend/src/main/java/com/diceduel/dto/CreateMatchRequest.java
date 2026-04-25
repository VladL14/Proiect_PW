package com.diceduel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateMatchRequest(
        @NotBlank String hostPlayerId,
        @Min(2) @Max(8) Integer maxPlayers
) {
}
