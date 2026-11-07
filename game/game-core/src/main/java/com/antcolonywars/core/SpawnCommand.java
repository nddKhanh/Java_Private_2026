package com.antcolonywars.core;

import java.util.Objects;

public record SpawnCommand(
        String commandId,
        String actorTeamId,
        String unitId,
        String displayName,
        String unitType,
        Direction direction
) implements ActionCommand {
    public SpawnCommand {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(actorTeamId, "actorTeamId");
        Objects.requireNonNull(unitId, "unitId");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(unitType, "unitType");
        Objects.requireNonNull(direction, "direction");
    }

    public SpawnCommand(String commandId, String actorTeamId, String unitId, String displayName, Direction direction) {
        this(commandId, actorTeamId, unitId, displayName, UnitType.WORKER.name(), direction);
    }
}
