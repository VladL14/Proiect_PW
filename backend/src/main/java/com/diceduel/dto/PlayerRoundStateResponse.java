package com.diceduel.dto;

import com.diceduel.entity.DiceFace;
import java.util.List;

public record PlayerRoundStateResponse(
        String playerId,
        List<DiceFace> dice,
        List<Boolean> locked,
        Integer rollsCount,
        java.util.Map<Integer, String> diceTargets
) {
}
