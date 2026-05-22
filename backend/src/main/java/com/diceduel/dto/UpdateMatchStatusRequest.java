package com.diceduel.dto;

import com.diceduel.entity.MatchStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMatchStatusRequest(
        @NotNull MatchStatus status
) {
}
