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
    public static final String MATCH_ID_ATTRIBUTE = "matchId";

    private final AccountService accountService;
    private final MatchRegistry matchRegistry;
    private final GameProfileProperties profile;

    public TokenHandshakeInterceptor(
            AccountService accountService,
            MatchRegistry matchRegistry,
            GameProfileProperties profile
    ) {
        this.accountService = accountService;
        this.matchRegistry = matchRegistry;
        this.profile = profile;
    }

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String query = request.getURI().getRawQuery();
        String matchId = queryParameter(query, "matchId");
        if (matchId == null || matchId.isBlank()) {
            matchId = profile.matchId();
        }
        MatchCoordinator coordinator = matchRegistry.find(matchId).orElse(null);
        if (coordinator == null) {
            return false;
        }
        attributes.put(MATCH_ID_ATTRIBUTE, matchId);

        String token = queryParameter(query, "token");
        if (token == null) {
            return true;
        }
        String teamId = accountService.resolveTeamId(token);
        boolean teamExists = coordinator.state().teams().stream().anyMatch(team -> team.id().equals(teamId));
        if (teamId == null || !teamExists) {
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
