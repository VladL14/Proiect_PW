package com.diceduel.dto;

import com.diceduel.entity.DiceFace;

import java.util.List;

public record RoundPlayerStateResponse(
        String playerId,
        String playerName,
        List<DiceFace> dice,
        List<Boolean> locked,
        List<String> targetPlayerIds,
        Integer rollsCount,
        Boolean eliminated,
        Integer shieldCount
) {
}
