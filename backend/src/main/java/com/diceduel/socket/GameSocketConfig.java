package com.diceduel.socket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Registers the raw-JSON game protocol handler at {@code /ws/game}.
 *
 * <p>Origins are left open so that browser clients, the Android client, an
 * A-Frame scene or a test bot can all connect to the same real-time channel.
 */
@Configuration
@EnableWebSocket
public class GameSocketConfig implements WebSocketConfigurer {

    private final GameSocketHandler gameSocketHandler;

    public GameSocketConfig(GameSocketHandler gameSocketHandler) {
        this.gameSocketHandler = gameSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(gameSocketHandler, "/ws/game")
                .setAllowedOriginPatterns("*");
    }
}
