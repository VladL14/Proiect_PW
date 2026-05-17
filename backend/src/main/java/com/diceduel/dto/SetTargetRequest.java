package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;

public record SetTargetRequest(
        @NotBlank(message = "Player ID must be provided")
        String playerId,

        @jakarta.validation.constraints.NotNull(message = "Dice targets map must be provided")
        java.util.Map<Integer, String> diceTargets
) {
}
