package com.diceduel.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Structured, machine-readable replay of a match.
 *
 * <p>Contains everything required to reconstruct the match: identity, the
 * participants, the initial state, the ordered list of timestamped actions and
 * the final result. This is the JSON counterpart of the legacy plain-text
 * replay export.
 *
 * @param matchId       match identifier
 * @param status        final/current match status
 * @param participants  players that took part
 * @param initialState  starting hearts/tokens for each participant
 * @param actions       chronological, timestamped action timeline
 * @param winnerId      winner identifier (nullable)
 * @param winnerName    winner display name (nullable)
 * @param finishedAt    completion time (nullable for unfinished matches)
 */
public record ReplayResponse(
        String matchId,
        String status,
        List<ParticipantDto> participants,
        List<PlayerSnapshotDto> initialState,
        List<ReplayActionDto> actions,
        String winnerId,
        String winnerName,
        LocalDateTime finishedAt
) {
}
