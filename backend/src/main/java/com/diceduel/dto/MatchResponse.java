package com.diceduel.dto;

import com.diceduel.entity.MatchStatus;

import java.util.List;

public record MatchResponse(
        String id,
        MatchStatus status,
        List<PlayerResponse> players
) {
}
