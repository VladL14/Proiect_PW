package com.diceduel.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateMatchRequest(
        @NotNull @Min(2) @Max(8) Integer maxPlayers
) {
}
