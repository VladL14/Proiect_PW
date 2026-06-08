package com.diceduel.socket;

import com.diceduel.dto.EmoteResponse;
import com.diceduel.dto.MatchStateResponse;
import com.diceduel.service.MatchService;
import com.diceduel.social.EmoteBroadcastEvent;
import com.diceduel.social.EmoteService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Raw-JSON WebSocket endpoint that implements the separate Dice Duel game
 * protocol at {@code /ws/game}.
 *
 * <p>The protocol is message-oriented: every frame is a JSON object with a
 * {@code type} discriminator. The authoritative game logic still lives in the
 * REST/service layer; this channel provides the real-time fan-out so that all
 * participants of a match receive lobby, state-sync and emote updates without
 * polling. Supported inbound types:
 * {@code CONNECT, JOIN_LOBBY, START_GAME, ACTION, STATE_SYNC, EMOTE}. Outbound
 * types include {@code CONNECT_ACK, STATE, EMOTE, GAME_OVER, ERROR} plus relays
 * of the lobby/action messages.
 */
@Component
public class GameSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final EmoteService emoteService;
    private final MatchService matchService;

    /** matchId -> active sessions in that match room. */
    private final ConcurrentHashMap<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();

    public GameSocketHandler(ObjectMapper objectMapper, EmoteService emoteService, MatchService matchService) {
        this.objectMapper = objectMapper;
        this.emoteService = emoteService;
        this.matchService = matchService;
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        try {
            JsonNode payload = objectMapper.readTree(message.getPayload());
            String type = text(payload, "type");
            if (type == null) {
                sendError(session, "Missing message type");
                return;
            }
            switch (type.toUpperCase()) {
                case "CONNECT" -> handleConnect(session, payload);
                case "JOIN_LOBBY", "START_GAME", "ACTION" -> relay(session, payload, type.toUpperCase());
                case "STATE_SYNC" -> handleStateSync(session, payload);
                case "EMOTE" -> handleEmote(session, payload);
                default -> sendError(session, "Unknown message type: " + type);
            }
        } catch (IOException e) {
            sendError(session, "Invalid JSON message");
        } catch (RuntimeException e) {
            sendError(session, e.getMessage());
        }
    }

    private void handleConnect(WebSocketSession session, JsonNode payload) {
        String matchId = text(payload, "matchId");
        String playerId = text(payload, "playerId");
        if (matchId == null) {
            sendError(session, "CONNECT requires a matchId");
            return;
        }
        session.getAttributes().put("matchId", matchId);
        session.getAttributes().put("playerId", playerId);
        rooms.computeIfAbsent(matchId, key -> ConcurrentHashMap.newKeySet()).add(session);

        ObjectNode ack = objectMapper.createObjectNode();
        ack.put("type", "CONNECT_ACK");
        ack.put("matchId", matchId);
        ack.put("playerId", playerId);
        ack.put("connectedClients", rooms.getOrDefault(matchId, Set.of()).size());
        send(session, ack);
    }

    private void handleStateSync(WebSocketSession session, JsonNode payload) {
        String matchId = roomOf(session, payload);
        if (matchId == null) {
            sendError(session, "STATE_SYNC requires a matchId");
            return;
        }
        MatchStateResponse state = matchService.findMatchState(matchId);
        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "STATE");
        message.set("state", objectMapper.valueToTree(state));
        broadcast(matchId, message);
    }

    private void handleEmote(WebSocketSession session, JsonNode payload) {
        String matchId = roomOf(session, payload);
        String playerId = text(payload, "playerId");
        String emote = text(payload, "emote");
        if (matchId == null || playerId == null || emote == null) {
            sendError(session, "EMOTE requires matchId, playerId and emote");
            return;
        }
        // Validation, membership check, cooldown and the broadcast event all
        // happen inside the service; the @EventListener below does the fan-out.
        emoteService.sendEmote(matchId, playerId, emote);
    }

    private void relay(WebSocketSession session, JsonNode payload, String type) {
        String matchId = roomOf(session, payload);
        if (matchId == null) {
            sendError(session, type + " requires a matchId");
            return;
        }
        ObjectNode message = payload.deepCopy();
        message.put("type", type);
        broadcast(matchId, message);
    }

    /**
     * Relays accepted emotes to the match room. Triggered by the social layer.
     */
    @EventListener
    public void onEmoteBroadcast(EmoteBroadcastEvent event) {
        EmoteResponse emote = event.emote();
        ObjectNode message = objectMapper.createObjectNode();
        message.put("type", "EMOTE");
        message.set("emote", objectMapper.valueToTree(emote));
        broadcast(emote.matchId(), message);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        Object matchId = session.getAttributes().get("matchId");
        if (matchId instanceof String room) {
            Set<WebSocketSession> sessions = rooms.get(room);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    rooms.remove(room);
                }
            }
        }
    }

    private void broadcast(String matchId, JsonNode message) {
        Set<WebSocketSession> sessions = rooms.get(matchId);
        if (sessions == null) {
            return;
        }
        for (WebSocketSession session : sessions) {
            send(session, message);
        }
    }

    private void send(WebSocketSession session, JsonNode message) {
        if (!session.isOpen()) {
            return;
        }
        try {
            String text = objectMapper.writeValueAsString(message);
            synchronized (session) {
                session.sendMessage(new TextMessage(text));
            }
        } catch (IOException ignored) {
            // Drop the frame if the socket is no longer writable.
        }
    }

    private void sendError(WebSocketSession session, String reason) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("type", "ERROR");
        error.put("message", reason == null ? "Unexpected error" : reason);
        send(session, error);
    }

    private String roomOf(WebSocketSession session, JsonNode payload) {
        String fromPayload = text(payload, "matchId");
        if (fromPayload != null) {
            return fromPayload;
        }
        Object attribute = session.getAttributes().get("matchId");
        return attribute instanceof String matchId ? matchId : null;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }
}
