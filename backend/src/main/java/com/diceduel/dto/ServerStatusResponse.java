package com.diceduel.dto;

import java.time.Instant;

/**
 * Administrative snapshot of the server state for the admin dashboard.
 *
 * @param totalAccounts       number of registered accounts/players
 * @param adminCount          number of accounts with the ADMIN role
 * @param totalMatches        number of matches currently stored
 * @param waitingMatches      matches in WAITING status
 * @param readyMatches        matches in READY status
 * @param inProgressMatches   matches in IN_PROGRESS status
 * @param finishedMatches     matches in FINISHED status
 * @param recordedHistory     number of matches recorded in the history
 * @param generatedAt         time the snapshot was produced
 */
public record ServerStatusResponse(
        long totalAccounts,
        long adminCount,
        long totalMatches,
        long waitingMatches,
        long readyMatches,
        long inProgressMatches,
        long finishedMatches,
        long recordedHistory,
        Instant generatedAt
) {
}
