package com.diceduel.social;

import com.diceduel.dto.EmoteResponse;

/**
 * Application event published whenever a valid emote is accepted. The WebSocket
 * layer listens for it and relays the emote to the players connected to the
 * match room, which decouples the social service from the transport.
 */
public record EmoteBroadcastEvent(EmoteResponse emote) {
}
