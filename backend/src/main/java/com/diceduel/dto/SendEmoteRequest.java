package com.diceduel.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for sending an in-game emote.
 *
 * @param playerId identifier of the player sending the emote
 * @param emote    emote code (validated against the server whitelist)
 */
public record SendEmoteRequest(
        @NotBlank String playerId,
        @NotBlank String emote
) {
}
