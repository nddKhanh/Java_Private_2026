package com.antcolonywars.desktop;

public record StudentSubmission(
        String className,
        String source,
        String pairingKey,
        String username,
        String password,
        String batchMode
) {
}
