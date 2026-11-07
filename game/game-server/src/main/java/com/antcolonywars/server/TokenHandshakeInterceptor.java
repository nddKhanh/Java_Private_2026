package com.antcolonywars.server;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class TokenHandshakeInterceptor implements HandshakeInterceptor {
    public static final String TEAM_ID_ATTRIBUTE = "teamId";

    private final GameProfileProperties profile;

    public TokenHandshakeInterceptor(GameProfileProperties profile) {
        this.profile = profile;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String token = queryParameter(request.getURI().getRawQuery(), "token");
        if (token == null) {
            return true;
        }
        String teamId = profile.teamIdForToken(token);
        if (teamId == null) {
            return false;
        }
        attributes.put(TEAM_ID_ATTRIBUTE, teamId);
        return true;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }

    private String queryParameter(String query, String name) {
        if (query == null || query.isBlank()) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && URLDecoder.decode(parts[0], StandardCharsets.UTF_8).equals(name)) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
