package com.antcolonywars.desktop;

import java.util.List;

public record PairingConfiguration(
        String serverUrl,
        String matchCode,
        String status,
        List<PairingTeam> teams
) {
    public record PairingTeam(String teamId, String displayName, String token) {
    }
}
