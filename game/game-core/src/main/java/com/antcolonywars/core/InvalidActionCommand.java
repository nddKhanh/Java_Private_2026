package com.antcolonywars.core;

import java.util.Objects;

public record InvalidActionCommand(String commandId, String actorTeamId, String unitId) implements ActionCommand {
    public InvalidActionCommand {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(actorTeamId, "actorTeamId");
        Objects.requireNonNull(unitId, "unitId");
    }
}
