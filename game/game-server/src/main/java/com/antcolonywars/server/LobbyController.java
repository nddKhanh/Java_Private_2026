package com.antcolonywars.server;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lobbies")
public final class LobbyController {
    private final LobbyService lobbyService;

    public LobbyController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LobbyService.LobbyView create(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody CreateLobbyRequest request
    ) {
        requireRequest(request);
        return lobbyService.create(bearerToken(authorization), request.mapScript());
    }

    @PostMapping("/join")
    public LobbyService.LobbyView join(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody JoinLobbyRequest request
    ) {
        requireRequest(request);
        return lobbyService.join(bearerToken(authorization), request.matchCode(), request.teamToken());
    }

    @GetMapping("/{matchCode}")
    public LobbyService.LobbyView view(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("matchCode") String matchCode
    ) {
        return lobbyService.view(bearerToken(authorization), matchCode);
    }

    @PostMapping("/{matchCode}/start")
    public LobbyService.LobbyView start(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable("matchCode") String matchCode
    ) {
        return lobbyService.start(bearerToken(authorization), matchCode);
    }

    private void requireRequest(Object request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "JSON request body is required");
        }
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() == 7) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_SESSION", "Bearer session token is required");
        }
        return authorization.substring(7).trim();
    }

    public record CreateLobbyRequest(String mapScript) {
    }

    public record JoinLobbyRequest(String matchCode, String teamToken) {
    }
}
