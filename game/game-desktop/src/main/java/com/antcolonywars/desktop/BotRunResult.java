package com.antcolonywars.desktop;

public record BotRunResult(
        int exitCode,
        boolean timedOut,
        boolean stopped,
        String standardOutput,
        String standardError
) {
    public boolean successful() {
        return exitCode == 0 && !timedOut && !stopped;
    }
}
