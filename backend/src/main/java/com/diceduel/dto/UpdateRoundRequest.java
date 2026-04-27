package com.diceduel.dto;

import com.diceduel.entity.DiceFace;
import com.diceduel.entity.RoundStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateRoundRequest(
        @NotNull RoundStatus status,
        @NotNull List<DiceFace> dice,
        @NotNull List<Boolean> locked
) {
}
