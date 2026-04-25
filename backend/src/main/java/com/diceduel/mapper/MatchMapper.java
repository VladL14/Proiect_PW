package com.diceduel.mapper;

import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.PlayerResponse;
import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.PlayerEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MatchMapper {

    private final PlayerMapper playerMapper;

    public MatchMapper(PlayerMapper playerMapper) {
        this.playerMapper = playerMapper;
    }

    public MatchResponse toResponse(MatchEntity match) {
        return new MatchResponse(
                match.getId(),
                match.getStatus(),
                toPlayerResponses(match.getPlayers())
        );
    }

    public MatchStateResponse toStateResponse(MatchEntity match) {
        return new MatchStateResponse(
                match.getCurrentRoundNumber(),
                toPlayerResponses(match.getPlayers())
        );
    }

    private List<PlayerResponse> toPlayerResponses(List<PlayerEntity> players) {
        return players.stream()
                .map(playerMapper::toResponse)
                .toList();
    }
}
