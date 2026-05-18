package com.diceduel.mapper;

import com.diceduel.dto.PlayerResponse;
import com.diceduel.dto.PlayerStatsResponse;
import com.diceduel.entity.PlayerEntity;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public PlayerResponse toResponse(PlayerEntity player) {
        return new PlayerResponse(
                player.getId(),
                player.getName(),
                player.getHearts(),
                player.getTokens(),
                player.getHearts() <= 0
        );
    }

    public PlayerStatsResponse toStatsResponse(PlayerEntity player) {
        return new PlayerStatsResponse(player.getWins(), player.getLosses());
    }
}
