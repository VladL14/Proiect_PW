package com.diceduel.mapper;

import com.diceduel.dto.AccountResponse;
import com.diceduel.dto.PlayerResponse;
import com.diceduel.dto.PlayerStatsResponse;
import com.diceduel.entity.AccountStatus;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    /**
     * Public base URL of this backend, used to build absolute avatar URLs.
     * Configurable through {@code app.public-base-url} (env var
     * APP_PUBLIC_BASE_URL); defaults to the local development host.
     */
    @Value("${app.public-base-url:http://localhost:3000}")
    private String publicBaseUrl;

   public PlayerResponse toResponse(PlayerEntity player) {
    String style = player.getAvatarStyle() != null ? player.getAvatarStyle() : "adventurer";
    String avatarUrl = player.getAvatarData() != null
            ? publicBaseUrl + "/api/players/" + player.getId() + "/avatar"
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

    public AccountResponse toAccountResponse(PlayerEntity player) {
        Role role = player.getRole() != null ? player.getRole() : Role.GUEST;
        AccountStatus status = player.getAccountStatus() != null ? player.getAccountStatus() : AccountStatus.ACTIVE;
        Integer matchesPlayed = player.getMatchesPlayed() != null ? player.getMatchesPlayed() : 0;
        return new AccountResponse(
                player.getId(),
                player.getUsername(),
                player.getEmail(),
                player.getName(),
                role,
                status,
                player.getCreatedAt(),
                matchesPlayed,
                player.getWins(),
                player.getLosses()
        );
    }
}
