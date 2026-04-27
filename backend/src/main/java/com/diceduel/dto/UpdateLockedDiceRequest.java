package com.diceduel.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateLockedDiceRequest(
        @NotNull List<Boolean> locked
) {
}
