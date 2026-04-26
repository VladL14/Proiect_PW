package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;

public record RollDiceRequest(
        @NotBlank String playerId
) {
}
