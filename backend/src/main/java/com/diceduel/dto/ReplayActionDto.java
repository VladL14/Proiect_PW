package com.diceduel.dto;

import java.time.Instant;

/**
 * One chronological action inside a replay timeline.
 *
 * @param sequence    monotonically increasing order index
 * @param timestamp   synthetic timestamp for the action
 * @param roundNumber round in which the action happened
 * @param type        action category (ROLL, COMBAT, ELIMINATION, MATCH_END, ...)
 * @param description human-readable description taken from the action log
 */
public record ReplayActionDto(
        int sequence,
        Instant timestamp,
        int roundNumber,
        String type,
        String description
) {
}
