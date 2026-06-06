package com.diceduel.dto;

/**
 * Snapshot of a player's resources at a given point of a replay.
 */
public record PlayerSnapshotDto(
        String playerId,
        String name,
        Integer hearts,
        Integer tokens
) {
}
