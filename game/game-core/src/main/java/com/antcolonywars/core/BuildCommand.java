package com.antcolonywars.core;

import java.util.Objects;

public record BuildCommand(
        String commandId,
        String actorTeamId,
        String unitId,
        String buildingId,
        BuildingType buildingType,
        Direction direction
) implements ActionCommand {
    public BuildCommand {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(actorTeamId, "actorTeamId");
        Objects.requireNonNull(unitId, "unitId");
        Objects.requireNonNull(buildingId, "buildingId");
        Objects.requireNonNull(buildingType, "buildingType");
        Objects.requireNonNull(direction, "direction");
    }
}
