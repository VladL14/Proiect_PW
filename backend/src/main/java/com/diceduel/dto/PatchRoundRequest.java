package com.diceduel.dto;

import com.diceduel.entity.RoundStatus;
import java.util.List;

public record PatchRoundRequest(
        RoundStatus status,
        List<PlayerRoundStateRequest> playerStates
) {
}
