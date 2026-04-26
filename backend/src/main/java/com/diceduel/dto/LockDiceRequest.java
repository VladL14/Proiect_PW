package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record LockDiceRequest(
        @NotBlank String playerId,
        @NotNull List<Integer> lockedIndexes
) {
}
