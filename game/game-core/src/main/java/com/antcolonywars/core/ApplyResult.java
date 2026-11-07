package com.antcolonywars.core;

import java.util.List;
import java.util.Objects;

public record ApplyResult(MatchState state, List<GameEvent> events, ErrorCode error) {
    public ApplyResult {
        Objects.requireNonNull(state, "state");
        events = List.copyOf(events);
    }

    public boolean accepted() {
        return error == null;
    }

    public static ApplyResult success(MatchState state, List<GameEvent> events) {
        return new ApplyResult(state, events, null);
    }

    public static ApplyResult failure(MatchState state, ErrorCode error) {
        return new ApplyResult(state, List.of(), Objects.requireNonNull(error, "error"));
    }
}
