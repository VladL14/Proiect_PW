package com.diceduel.mapper;

import com.diceduel.dto.PlayerRoundStateResponse;
import com.diceduel.dto.RoundResponse;
import com.diceduel.entity.PlayerRoundStateEntity;
import com.diceduel.entity.RoundEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class RoundMapper {

    public RoundResponse toResponse(RoundEntity round) {
        return new RoundResponse(
                round.getId(),
                round.getStatus(),
                round.getPlayerStates().stream().map(this::toPlayerStateResponse).collect(Collectors.toList())
        );
    }

    private PlayerRoundStateResponse toPlayerStateResponse(PlayerRoundStateEntity state) {
        return new PlayerRoundStateResponse(
                state.getPlayer().getId(),
                new ArrayList<>(state.getDice()),
                new ArrayList<>(state.getLocked()),
                state.getRollsCount(),
                new java.util.HashMap<>(state.getDiceTargets())
        );
    }
}
