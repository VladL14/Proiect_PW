package com.diceduel.mapper;

import com.diceduel.dto.MatchResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.dto.PlayerResponse;
import com.diceduel.dto.RoundResponse;
import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.RoundEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class MatchMapper {

    private final PlayerMapper playerMapper;
    private final RoundMapper roundMapper;

    public MatchMapper(PlayerMapper playerMapper, RoundMapper roundMapper) {
        this.playerMapper = playerMapper;
        this.roundMapper = roundMapper;
    }

    public MatchResponse toResponse(MatchEntity match) {
        return new MatchResponse(
                match.getId(),
                match.getStatus(),
                toPlayerResponses(match.getPlayers()),
                match.getWinnerPlayerId()
        );
    }

    public MatchStateResponse toStateResponse(MatchEntity match) {
        Optional<RoundEntity> currentRound = findCurrentRound(match);
        Optional<RoundEntity> latestLoggedRound = findLatestLoggedRound(match).or(() -> currentRound);
        RoundResponse currentRoundState = currentRound.map(roundMapper::toResponse).orElse(null);
        PlayerEntity winner = findWinner(match);

        return new MatchStateResponse(
                match.getId(),
                match.getStatus(),
                match.getCurrentRoundNumber(),
                findCurrentTurnPlayerId(match),
                toPlayerResponses(match.getPlayers()),
                currentRoundState,
                match.getWinnerPlayerId(),
                winner == null ? null : winner.getName(),
                Math.toIntExact(match.getPlayers().stream().filter(player -> player.getHearts() > 0).count()),
                latestLoggedRound.map(RoundEntity::getRoundSummary).orElse(null),
                latestLoggedRound.map(round -> List.copyOf(round.getActionLogs())).orElse(List.of())
        );
    }

    private List<PlayerResponse> toPlayerResponses(List<PlayerEntity> players) {
        return players.stream()
                .map(playerMapper::toResponse)
                .toList();
    }

    private Optional<RoundEntity> findCurrentRound(MatchEntity match) {
        return match.getRounds()
                .stream()
                .filter(round -> round.getRoundNumber().equals(match.getCurrentRoundNumber()))
                .findFirst()
                .or(() -> match.getRounds()
                        .stream()
                        .max(Comparator.comparing(RoundEntity::getRoundNumber)));
    }

    private Optional<RoundEntity> findLatestLoggedRound(MatchEntity match) {
        return match.getRounds()
                .stream()
                .filter(round -> round.getActionLogs() != null && !round.getActionLogs().isEmpty())
                .max(Comparator.comparing(RoundEntity::getRoundNumber));
    }

    private String findCurrentTurnPlayerId(MatchEntity match) {
        return match.getPlayers()
                .stream()
                .filter(player -> player.getHearts() > 0)
                .map(PlayerEntity::getId)
                .findFirst()
                .orElse(null);
    }

    private PlayerEntity findWinner(MatchEntity match) {
        if (match.getWinnerPlayerId() == null) {
            return null;
        }
        return match.getPlayers()
                .stream()
                .filter(player -> player.getId().equals(match.getWinnerPlayerId()))
                .findFirst()
                .orElse(null);
    }
}
