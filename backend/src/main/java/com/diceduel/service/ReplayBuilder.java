package com.diceduel.service;

import com.diceduel.dto.ParticipantDto;
import com.diceduel.dto.PlayerSnapshotDto;
import com.diceduel.dto.ReplayActionDto;
import com.diceduel.dto.ReplayResponse;
import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.entity.RoundEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds a structured {@link ReplayResponse} out of the persisted match and
 * round data. Shared by the live replay export endpoint and the match-history
 * recorder so both produce the exact same reconstruction format.
 */
@Component
public class ReplayBuilder {

    private static final int STARTING_HEARTS = 3;
    private static final int STARTING_TOKENS = 0;

    public ReplayResponse build(MatchEntity match) {
        List<ParticipantDto> participants = match.getPlayers().stream()
                .map(player -> new ParticipantDto(player.getId(), player.getName()))
                .toList();

        List<PlayerSnapshotDto> initialState = match.getPlayers().stream()
                .map(player -> new PlayerSnapshotDto(
                        player.getId(),
                        player.getName(),
                        STARTING_HEARTS,
                        STARTING_TOKENS))
                .toList();

        LocalDateTime base = match.getCreatedAt() != null ? match.getCreatedAt() : LocalDateTime.now();
        Instant baseInstant = base.atZone(ZoneId.systemDefault()).toInstant();

        List<ReplayActionDto> actions = new ArrayList<>();
        int sequence = 0;
        List<RoundEntity> rounds = match.getRounds().stream()
                .sorted(Comparator.comparing(RoundEntity::getRoundNumber))
                .toList();
        for (RoundEntity round : rounds) {
            List<String> logs = round.getActionLogs();
            if (logs == null) {
                continue;
            }
            for (String log : logs) {
                actions.add(new ReplayActionDto(
                        sequence,
                        baseInstant.plusSeconds(sequence),
                        round.getRoundNumber(),
                        classify(log),
                        log
                ));
                sequence++;
            }
        }

        String winnerName = match.getPlayers().stream()
                .filter(player -> player.getId().equals(match.getWinnerPlayerId()))
                .map(PlayerEntity::getName)
                .findFirst()
                .orElse(null);

        return new ReplayResponse(
                match.getId(),
                match.getStatus() == null ? null : match.getStatus().name(),
                participants,
                initialState,
                actions,
                match.getWinnerPlayerId(),
                winnerName,
                match.getStatus() != null && match.getStatus().name().equals("FINISHED") ? LocalDateTime.now() : null
        );
    }

    private String classify(String log) {
        String lower = log.toLowerCase();
        if (lower.contains("rolled")) {
            return "ROLL";
        }
        if (lower.contains("locked")) {
            return "LOCK";
        }
        if (lower.contains("chose targets")) {
            return "TARGET";
        }
        if (lower.contains("eliminated")) {
            return "ELIMINATION";
        }
        if (lower.contains("match ended") || lower.contains("won the duel")) {
            return "MATCH_END";
        }
        if (lower.contains("attacked") || lower.contains("stole") || lower.contains("blocked") || lower.contains("steal")) {
            return "COMBAT";
        }
        return "EVENT";
    }
}
