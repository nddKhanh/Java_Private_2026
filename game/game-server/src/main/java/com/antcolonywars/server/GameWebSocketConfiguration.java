package com.antcolonywars.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class GameWebSocketConfiguration implements WebSocketConfigurer {
    private final GameWebSocketHandler handler;
    private final TokenHandshakeInterceptor tokenInterceptor;

    public GameWebSocketConfiguration(
            GameWebSocketHandler handler,
            TokenHandshakeInterceptor tokenInterceptor
    ) {
        this.handler = handler;
        this.tokenInterceptor = tokenInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/game")
                .addInterceptors(tokenInterceptor)
                .setAllowedOrigins("*");
    }
}
