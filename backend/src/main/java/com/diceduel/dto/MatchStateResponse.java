package com.diceduel.dto;

import java.util.List;

public record MatchStateResponse(
        Integer currentRound,
        List<PlayerResponse> players
) {
}
