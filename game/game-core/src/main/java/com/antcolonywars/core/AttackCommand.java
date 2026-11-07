package com.antcolonywars.core;

import java.util.Objects;

public record AttackCommand(String commandId, String actorTeamId, String unitId, String targetId)
        implements ActionCommand {
    public AttackCommand {
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(actorTeamId, "actorTeamId");
        Objects.requireNonNull(unitId, "unitId");
        Objects.requireNonNull(targetId, "targetId");
    }
}
