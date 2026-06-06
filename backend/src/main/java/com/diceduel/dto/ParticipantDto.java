package com.diceduel.dto;

/**
 * Lightweight participant reference used in history and replay payloads.
 */
public record ParticipantDto(
        String id,
        String name
) {
}
