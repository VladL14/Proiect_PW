package com.diceduel.mapper;

import com.diceduel.dto.PlayerResponse;
import com.diceduel.dto.PlayerStatsResponse;
import com.diceduel.entity.PlayerEntity;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

   public PlayerResponse toResponse(PlayerEntity player) {
    String style = player.getAvatarStyle() != null ? player.getAvatarStyle() : "adventurer";
    String avatarUrl = player.getAvatarData() != null
            ? "http://localhost:3000/api/players/" + player.getId() + "/avatar"
            : "https://api.dicebear.com/9.x/" + style + "/svg?seed=" + player.getId();
    return new PlayerResponse(
            player.getId(),
            player.getName(),
            player.getHearts(),
            player.getTokens(),
            player.getHearts() <= 0,
            avatarUrl,
            style
    );
}

    public PlayerStatsResponse toStatsResponse(PlayerEntity player) {
        return new PlayerStatsResponse(player.getWins(), player.getLosses());
    }
}
