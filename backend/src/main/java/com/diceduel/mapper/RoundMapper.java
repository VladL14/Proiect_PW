package com.diceduel.mapper;

import com.diceduel.dto.RoundPlayerStateResponse;
import com.diceduel.dto.RoundResponse;
import com.diceduel.entity.DiceFace;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.RoundEntity;
import com.diceduel.entity.RoundPlayerStateEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class RoundMapper {

    public RoundResponse toResponse(RoundEntity round) {
        List<RoundPlayerStateResponse> playerStates = mapPlayerStates(round);

        return new RoundResponse(
                round.getId(),
                round.getRoundNumber(),
                round.getStatus(),
                copy(round.getDice()),
                copy(round.getLocked()),
                firstStateTargets(round),
                playerStates,
                round.getRoundSummary(),
                copy(round.getActionLogs())
        );
    }

    private List<RoundPlayerStateResponse> mapPlayerStates(RoundEntity round) {
        Map<String, PlayerEntity> playersById = round.getMatch().getPlayers()
                .stream()
                .collect(Collectors.toMap(PlayerEntity::getId, Function.identity()));

        return round.getPlayerStates()
                .stream()
                .sorted(Comparator.comparing(RoundPlayerStateEntity::getTurnOrder))
                .map(state -> toPlayerStateResponse(state, playersById.get(state.getPlayerId())))
                .toList();
    }

    private RoundPlayerStateResponse toPlayerStateResponse(
            RoundPlayerStateEntity state,
            PlayerEntity player
    ) {
        String playerName = player == null ? "Unknown Player" : player.getName();
        boolean eliminated = player != null && player.getHearts() <= 0;
        int shieldCount = Math.toIntExact(state.getDice()
                .stream()
                .filter(face -> face == DiceFace.SHIELD)
                .count());

        return new RoundPlayerStateResponse(
                state.getPlayerId(),
                playerName,
                copy(state.getDice()),
                copy(state.getLocked()),
                normalizeTargets(state.getTargetPlayerIds()),
                state.getRollsCount(),
                eliminated,
                shieldCount
        );
    }

    private List<String> firstStateTargets(RoundEntity round) {
        return round.getPlayerStates()
                .stream()
                .min(Comparator.comparing(RoundPlayerStateEntity::getTurnOrder))
                .map(state -> normalizeTargets(state.getTargetPlayerIds()))
                .orElse(List.of());
    }

    private <T> List<T> copy(List<T> values) {
        return values == null ? List.of() : new ArrayList<>(values);
    }

    private List<String> normalizeTargets(List<String> targetPlayerIds) {
        if (targetPlayerIds == null) {
            return List.of();
        }
        return targetPlayerIds.stream()
                .map(value -> value == null || value.isBlank() ? null : value)
                .toList();
    }
}
