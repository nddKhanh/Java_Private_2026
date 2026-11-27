package com.antcolonywars.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class GameWebSocketConfiguration implements WebSocketConfigurer {
    /** Fits the largest valid batch and a full snapshot; the servlet default is 8 KB. */
    static final int MAX_TEXT_MESSAGE_BYTES = 1024 * 1024;

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

    @Bean
    public ServletServerContainerFactoryBean webSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(MAX_TEXT_MESSAGE_BYTES);
        return container;
    }
}
