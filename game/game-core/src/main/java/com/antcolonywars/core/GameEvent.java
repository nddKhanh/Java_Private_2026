package com.antcolonywars.core;

import java.util.Objects;

public record GameEvent(
        long sequence,
        long revision,
        String commandId,
        String action,
        String actorTeamId,
        String unitId,
        String targetId
) {
    public GameEvent {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(actorTeamId, "actorTeamId");
        Objects.requireNonNull(unitId, "unitId");
    }
}
