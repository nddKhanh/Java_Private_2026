package com.antcolonywars.core;

import java.util.Objects;

public record SpawnCommand(
        String commandId,
        String actorTeamId,
        String unitId,
        String displayName,
        Direction direction
) implements ActionCommand {
    public SpawnCommand {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(actorTeamId, "actorTeamId");
        Objects.requireNonNull(unitId, "unitId");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(direction, "direction");
    }
}
