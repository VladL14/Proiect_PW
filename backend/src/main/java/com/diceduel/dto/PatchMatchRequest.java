package com.diceduel.dto;

import com.diceduel.entity.MatchStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PatchMatchRequest(
        MatchStatus status,
        @Min(2) @Max(8) Integer maxPlayers
) {
}
