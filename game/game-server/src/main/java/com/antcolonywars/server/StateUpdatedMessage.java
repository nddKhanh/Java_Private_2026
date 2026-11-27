package com.antcolonywars.server;

import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.MatchState;

import java.util.List;
import java.util.Map;

/**
 * Confirmed match state for one connection.
 * viewerTeamId is the team this connection plays for, or null for a spectator;
 * queues holds the pending commands of the viewer's own bots keyed by bot id;
 * rejections lists the viewer's commands that failed on their turn during this update.
 */
public record StateUpdatedMessage(
        String type,
        int schemaVersion,
        String matchId,
        long revision,
        long sequence,
        String viewerTeamId,
        MatchState state,
        List<GameEvent> events,
        Map<String, List<QueuedCommandView>> queues,
        List<RejectionMessage> rejections
) {
    public StateUpdatedMessage {
        events = List.copyOf(events);
        queues = Map.copyOf(queues);
        rejections = List.copyOf(rejections);
    }

    public static StateUpdatedMessage from(
            String viewerTeamId,
            MatchState state,
            List<GameEvent> events,
            Map<String, List<QueuedCommandView>> queues,
            List<RejectionMessage> rejections
    ) {
        return new StateUpdatedMessage(
                "STATE_UPDATED",
                GameWebSocketHandler.SCHEMA_VERSION,
                state.matchId(),
                state.revision(),
                state.eventSequence(),
                viewerTeamId,
                state,
                events,
                queues,
                rejections
        );
    }
}
