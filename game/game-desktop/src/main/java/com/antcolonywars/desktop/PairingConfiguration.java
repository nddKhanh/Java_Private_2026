package com.antcolonywars.desktop;

public record PairingConfiguration(
        String serverUrl,
        String matchCode,
        String status,
        String teamId,
        String controlToken
) {
}
