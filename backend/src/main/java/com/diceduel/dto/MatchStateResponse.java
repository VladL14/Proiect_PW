package com.diceduel.dto;

import com.diceduel.entity.MatchStatus;

import java.util.List;

public record MatchStateResponse(
        String matchId,
        MatchStatus matchStatus,
        Integer currentRound,
        String currentTurnPlayerId,
        List<PlayerResponse> players,
        RoundResponse currentRoundState,
        String winnerPlayerId,
        String winnerName,
        Integer alivePlayerCount,
        String roundSummary,
        List<String> actionLogs
) {
}
