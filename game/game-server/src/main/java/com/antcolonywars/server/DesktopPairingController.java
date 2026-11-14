package com.antcolonywars.server;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/desktop/pairing")
public final class DesktopPairingController {
    private final LobbyService lobbyService;

    public DesktopPairingController(LobbyService lobbyService) {
        this.lobbyService = lobbyService;
    }

    @GetMapping("/{key}")
    public LobbyService.PairingView pair(
            @PathVariable("key") String key,
            HttpServletRequest request
    ) {
        return lobbyService.pair(key, request.getServerName(), request.getServerPort());
    }
}
