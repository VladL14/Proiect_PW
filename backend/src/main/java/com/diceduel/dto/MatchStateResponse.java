package com.diceduel.dto;

import java.util.List;

public record MatchStateResponse(
        Integer currentRound,
        String currentTurnPlayerId,
        List<PlayerResponse> players
) {
}
