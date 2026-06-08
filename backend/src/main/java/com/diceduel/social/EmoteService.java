package com.diceduel.social;

import com.diceduel.dto.EmoteResponse;
import com.diceduel.entity.MatchEntity;
import com.diceduel.entity.PlayerEntity;
import com.diceduel.exception.BadRequestException;
import com.diceduel.exception.CooldownException;
import com.diceduel.exception.ForbiddenException;
import com.diceduel.exception.ResourceNotFoundException;
import com.diceduel.repository.MatchRepository;
import com.diceduel.security.CurrentUser;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-game social emotes with membership validation and anti-spam protection.
 *
 * <p>Emotes are ephemeral: they are kept in a small in-memory ring buffer per
 * match so the frontend can poll recent emotes, and they are also pushed to the
 * WebSocket room through an application event. The backend is the source of
 * truth for two rules: the sender must be a participant of the match, and the
 * sender must respect a per-player cooldown so emotes cannot be spammed.
 */
@Service
public class EmoteService {

    /** Whitelisted emote codes. Anything else is rejected with HTTP 400. */
    private static final Set<String> ALLOWED_EMOTES = Set.of(
            "WAVE", "GG", "LAUGH", "ANGRY", "THINK", "NICE", "FIRE", "SKULL", "CLAP", "CRY"
    );

    private static final long COOLDOWN_MS = 3_000L;
    private static final int MAX_BUFFER = 100;

    private final MatchRepository matchRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final ConcurrentHashMap<String, Deque<EmoteResponse>> emotesByMatch = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> lastEmoteAt = new ConcurrentHashMap<>();

    public EmoteService(MatchRepository matchRepository, ApplicationEventPublisher eventPublisher) {
        this.matchRepository = matchRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Validates and registers an emote, then broadcasts it.
     *
     * @param matchId  match identifier
     * @param playerId sender identifier
     * @param emote    emote code
     * @return the accepted emote event
     */
    @Transactional(readOnly = true)
    public EmoteResponse sendEmote(String matchId, String playerId, String emote) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found: " + matchId));

        PlayerEntity sender = match.getPlayers().stream()
                .filter(player -> player.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Player is not part of this match"));

        // Authenticated users may only emote as themselves (admins are exempt).
        if (CurrentUser.isAuthenticated() && !CurrentUser.isAdmin()
                && !playerId.equals(CurrentUser.playerId())) {
            throw new ForbiddenException("You can only send emotes as yourself");
        }

        String normalized = emote == null ? "" : emote.trim().toUpperCase();
        if (!ALLOWED_EMOTES.contains(normalized)) {
            throw new BadRequestException("Unsupported emote: " + emote);
        }

        long now = System.currentTimeMillis();
        String cooldownKey = matchId + ":" + playerId;
        Long previous = lastEmoteAt.get(cooldownKey);
        if (previous != null && now - previous < COOLDOWN_MS) {
            long waitMs = COOLDOWN_MS - (now - previous);
            throw new CooldownException("Please wait " + Math.ceil(waitMs / 1000.0) + "s before sending another emote");
        }
        lastEmoteAt.put(cooldownKey, now);

        EmoteResponse response = new EmoteResponse(
                UUID.randomUUID().toString(),
                matchId,
                playerId,
                sender.getName(),
                normalized,
                now
        );

        Deque<EmoteResponse> buffer = emotesByMatch.computeIfAbsent(matchId, key -> new ArrayDeque<>());
        synchronized (buffer) {
            buffer.addLast(response);
            while (buffer.size() > MAX_BUFFER) {
                buffer.removeFirst();
            }
        }

        eventPublisher.publishEvent(new EmoteBroadcastEvent(response));
        return response;
    }

    /**
     * Returns the recent emotes of a match, optionally only those after a
     * given epoch-millis timestamp (used by the frontend polling fallback).
     *
     * @param matchId match identifier
     * @param since   epoch-millis lower bound (exclusive); 0 returns all buffered
     * @return recent emotes in chronological order
     */
    public List<EmoteResponse> getRecentEmotes(String matchId, long since) {
        Deque<EmoteResponse> buffer = emotesByMatch.get(matchId);
        if (buffer == null) {
            return List.of();
        }
        synchronized (buffer) {
            return buffer.stream()
                    .filter(emote -> emote.timestamp() > since)
                    .toList();
        }
    }
}
