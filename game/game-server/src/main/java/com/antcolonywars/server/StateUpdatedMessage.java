package com.antcolonywars.server;

import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.MatchState;
import java.util.List;

public record StateUpdatedMessage(
        String type,
        int schemaVersion,
        String matchId,
        long revision,
        long sequence,
        MatchState state,
        List<GameEvent> events
) {
    public StateUpdatedMessage {
        events = List.copyOf(events);
    }

    public static StateUpdatedMessage from(MatchState state, List<GameEvent> events) {
        return new StateUpdatedMessage(
                "STATE_UPDATED",
                GameWebSocketHandler.SCHEMA_VERSION,
                state.matchId(),
                state.revision(),
                state.eventSequence(),
                state,
                events
        );
    }
}
