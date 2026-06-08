package com.diceduel.service.impl;

import com.diceduel.dto.MatchHistoryResponse;
import com.diceduel.dto.ParticipantDto;
import com.diceduel.dto.ReplayResponse;
import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.MatchHistoryEntity;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.exception.ForbiddenException;
import com.diceduel.exception.UnauthorizedException;
import com.diceduel.repository.MatchHistoryRepository;
import com.diceduel.repository.PlayerRepository;
import com.diceduel.security.AuthSession;
import com.diceduel.security.CurrentUser;
import com.diceduel.service.MatchHistoryService;
import com.diceduel.service.ReplayBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MatchHistoryServiceImpl implements MatchHistoryService {

    /** Delimiter that will not appear inside UUIDs or normal display names. */
    private static final String SEP = "<|>";

    private final MatchHistoryRepository matchHistoryRepository;
    private final PlayerRepository playerRepository;
    private final ReplayBuilder replayBuilder;
    private final ObjectMapper objectMapper;

    public MatchHistoryServiceImpl(
            MatchHistoryRepository matchHistoryRepository,
            PlayerRepository playerRepository,
            ReplayBuilder replayBuilder,
            ObjectMapper objectMapper
    ) {
        this.matchHistoryRepository = matchHistoryRepository;
        this.playerRepository = playerRepository;
        this.replayBuilder = replayBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void recordFinishedMatch(MatchEntity match, List<String> events) {
        if (matchHistoryRepository.findByMatchId(match.getId()).isPresent()) {
            return;
        }

        MatchHistoryEntity history = new MatchHistoryEntity();
        history.setId(UUID.randomUUID().toString());
        history.setMatchId(match.getId());
        history.setParticipantIdsCsv(join(match.getPlayers().stream().map(PlayerEntity::getId).toList()));
        history.setParticipantNamesCsv(join(match.getPlayers().stream().map(PlayerEntity::getName).toList()));
        history.setWinnerId(match.getWinnerPlayerId());
        history.setWinnerName(resolveWinnerName(match));
        history.setFinalStatus(match.getStatus() == null ? null : match.getStatus().name());

        LocalDateTime startedAt = match.getCreatedAt() != null ? match.getCreatedAt() : LocalDateTime.now();
        LocalDateTime finishedAt = LocalDateTime.now();
        history.setStartedAt(startedAt);
        history.setFinishedAt(finishedAt);
        history.setDurationSeconds(Math.max(0, Duration.between(startedAt, finishedAt).getSeconds()));

        history.setEvents(events == null ? "" : String.join("\n", events));
        history.setReplayJson(serializeReplay(match));

        matchHistoryRepository.save(history);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchHistoryResponse> findAll() {
        return matchHistoryRepository.findAllByOrderByFinishedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchHistoryResponse> findForPlayer(String playerId) {
        AuthSession session = CurrentUser.get()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
        if (!CurrentUser.isAdmin() && !session.playerId().equals(playerId)) {
            throw new ForbiddenException("You can only view your own match history");
        }
        return matchHistoryRepository.findAllByOrderByFinishedAtDesc()
                .stream()
                .filter(history -> splitValues(history.getParticipantIdsCsv()).contains(playerId))
                .map(this::toResponse)
                .toList();
    }

    private String serializeReplay(MatchEntity match) {
        try {
            ReplayResponse replay = replayBuilder.build(match);
            return objectMapper.writeValueAsString(replay);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private MatchHistoryResponse toResponse(MatchHistoryEntity history) {
        List<String> ids = splitValues(history.getParticipantIdsCsv());
        List<String> names = splitValues(history.getParticipantNamesCsv());
        List<ParticipantDto> participants = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            String name = i < names.size() ? names.get(i) : ids.get(i);
            participants.add(new ParticipantDto(ids.get(i), name));
        }
        List<String> events = history.getEvents() == null || history.getEvents().isBlank()
                ? List.of()
                : List.of(history.getEvents().split("\n"));
        return new MatchHistoryResponse(
                history.getId(),
                history.getMatchId(),
                participants,
                history.getWinnerId(),
                history.getWinnerName(),
                history.getFinalStatus(),
                history.getStartedAt(),
                history.getFinishedAt(),
                history.getDurationSeconds(),
                events,
                history.getReplayJson() != null && !history.getReplayJson().isBlank()
        );
    }

    private String resolveWinnerName(MatchEntity match) {
        if (match.getWinnerPlayerId() == null) {
            return null;
        }
        return match.getPlayers().stream()
                .filter(player -> player.getId().equals(match.getWinnerPlayerId()))
                .map(PlayerEntity::getName)
                .findFirst()
                .orElse(null);
    }

    private String join(List<String> values) {
        return String.join(SEP, values);
    }

    private List<String> splitValues(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return List.of(value.split(java.util.regex.Pattern.quote(SEP)));
    }
}
