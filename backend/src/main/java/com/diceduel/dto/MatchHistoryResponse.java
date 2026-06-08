package com.diceduel.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Read model for one finished match in the match history.
 *
 * @param id              history record identifier
 * @param matchId         identifier of the original match
 * @param participants    players that took part in the match
 * @param winnerId        winner identifier (null for a draw/aborted match)
 * @param winnerName      winner display name
 * @param finalStatus     final match status (usually FINISHED)
 * @param startedAt       match start time
 * @param finishedAt      match completion time
 * @param durationSeconds total match duration in seconds
 * @param events          important chronological events of the match
 * @param hasReplay       whether a downloadable replay is associated
 */
public record MatchHistoryResponse(
        String id,
        String matchId,
        List<ParticipantDto> participants,
        String winnerId,
        String winnerName,
        String finalStatus,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Long durationSeconds,
        List<String> events,
        boolean hasReplay
) {
}
