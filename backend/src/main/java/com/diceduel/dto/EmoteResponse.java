package com.diceduel.dto;

/**
 * A broadcast emote event.
 *
 * @param id         unique emote event identifier
 * @param matchId    match the emote belongs to
 * @param playerId   sender identifier
 * @param playerName sender display name
 * @param emote      emote code
 * @param timestamp  epoch-millis timestamp of the emote
 */
public record EmoteResponse(
        String id,
        String matchId,
        String playerId,
        String playerName,
        String emote,
        long timestamp
) {
}
