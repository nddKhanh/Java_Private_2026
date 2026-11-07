package com.antcolonywars.desktop;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

public record BotRunRequest(
        String className,
        Path classesDirectory,
        String serverUrl,
        String teamToken,
        Duration timeout,
        int maxHeapMegabytes,
        int maxOutputBytes
) {
    public BotRunRequest {
        Objects.requireNonNull(className, "className");
        Objects.requireNonNull(classesDirectory, "classesDirectory");
        Objects.requireNonNull(serverUrl, "serverUrl");
        Objects.requireNonNull(teamToken, "teamToken");
        Objects.requireNonNull(timeout, "timeout");
        if (className.isBlank() || serverUrl.isBlank() || teamToken.isBlank()) {
            throw new IllegalArgumentException("Class name, server URL and team token are required");
        }
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("Timeout must be positive");
        }
        if (maxHeapMegabytes < 16 || maxOutputBytes < 1024) {
            throw new IllegalArgumentException("Runner limits are too small");
        }
    }
}
