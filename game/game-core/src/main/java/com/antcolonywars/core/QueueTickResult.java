package com.antcolonywars.core;

import java.util.List;
import java.util.Objects;

/** Outcome of one simulation tick: the queue heads that ran, projectile and tower updates, and failed commands. */
public record QueueTickResult(MatchState state, List<GameEvent> events, List<CommandRejection> rejections) {
    public QueueTickResult {
        Objects.requireNonNull(state, "state");
        events = List.copyOf(events);
        rejections = List.copyOf(rejections);
    }

    public boolean changed() {
        return !events.isEmpty() || !rejections.isEmpty();
    }
}
