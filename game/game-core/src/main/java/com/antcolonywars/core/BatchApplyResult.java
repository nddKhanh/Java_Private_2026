package com.antcolonywars.core;

import java.util.List;
import java.util.Objects;

public record BatchApplyResult(
        MatchState state,
        List<GameEvent> events,
        ErrorCode error,
        int failedIndex,
        int appliedCount
) {
    public BatchApplyResult {
        Objects.requireNonNull(state, "state");
        events = List.copyOf(events);
    }

    public boolean accepted() {
        return error == null;
    }

    public static BatchApplyResult success(MatchState state, List<GameEvent> events, int appliedCount) {
        return new BatchApplyResult(state, events, null, -1, appliedCount);
    }

    public static BatchApplyResult failure(
            MatchState state,
            List<GameEvent> events,
            ErrorCode error,
            int failedIndex,
            int appliedCount
    ) {
        return new BatchApplyResult(
                state, events, Objects.requireNonNull(error, "error"), failedIndex, appliedCount
        );
    }
}
