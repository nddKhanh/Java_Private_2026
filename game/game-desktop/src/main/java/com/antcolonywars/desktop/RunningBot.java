package com.antcolonywars.desktop;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public final class RunningBot {
    private final Process process;
    private final CompletableFuture<BotRunResult> completion;
    private final AtomicBoolean stopped;

    RunningBot(Process process, CompletableFuture<BotRunResult> completion, AtomicBoolean stopped) {
        this.process = process;
        this.completion = completion;
        this.stopped = stopped;
    }

    public CompletableFuture<BotRunResult> completion() {
        return completion;
    }

    public boolean isAlive() {
        return process.isAlive();
    }

    public void stop() {
        if (process.isAlive()) {
            stopped.set(true);
            process.destroyForcibly();
        }
    }
}
