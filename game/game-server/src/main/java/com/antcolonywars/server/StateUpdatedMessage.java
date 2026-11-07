package com.antcolonywars.server;

import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.MatchState;

public record StateUpdatedMessage(
        String type,
        int schemaVersion,
        String matchId,
        long revision,
        long sequence,
        MatchState state,
        GameEvent event
) {
    public static StateUpdatedMessage from(MatchState state, GameEvent event) {
        return new StateUpdatedMessage(
                "STATE_UPDATED",
                GameWebSocketHandler.SCHEMA_VERSION,
                state.matchId(),
                state.revision(),
                state.eventSequence(),
                state,
                event
        );
    }
}
