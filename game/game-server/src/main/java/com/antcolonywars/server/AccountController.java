package com.antcolonywars.server;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public final class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountService.Account register(@RequestBody RegisterRequest request) {
        requireRequest(request);
        return accountService.register(request.username(), request.displayName(), request.password());
    }

    @PostMapping("/sessions")
    public AccountService.Session login(@RequestBody LoginRequest request) {
        requireRequest(request);
        return accountService.login(request.username(), request.password());
    }

    @GetMapping("/lobby/me")
    public AccountService.Account me(
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        return accountService.requireAccount(bearerToken(authorization));
    }

    @PutMapping("/lobby/membership")
    public AccountService.Account joinTeam(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody JoinTeamRequest request
    ) {
        requireRequest(request);
        return accountService.joinTeam(bearerToken(authorization), request.teamId());
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

    public record RegisterRequest(String username, String displayName, String password) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record JoinTeamRequest(String teamId) {
    }
}
