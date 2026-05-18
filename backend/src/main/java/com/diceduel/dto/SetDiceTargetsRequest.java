package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record SetDiceTargetsRequest(
        @NotBlank String playerId,
        @NotNull Map<Integer, String> diceTargets
) {
}
