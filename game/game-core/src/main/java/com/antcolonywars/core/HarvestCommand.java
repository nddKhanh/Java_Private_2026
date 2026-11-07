package com.antcolonywars.core;

import java.util.Objects;

public record HarvestCommand(String commandId, String actorTeamId, String unitId) implements ActionCommand {
    public HarvestCommand {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(actorTeamId, "actorTeamId");
        Objects.requireNonNull(unitId, "unitId");
    }
}
