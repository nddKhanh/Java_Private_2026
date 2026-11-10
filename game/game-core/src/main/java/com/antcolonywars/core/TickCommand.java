package com.antcolonywars.core;

import java.util.Objects;

public record TickCommand(String commandId) implements ActionCommand {
    public TickCommand {
        Objects.requireNonNull(commandId, "commandId");
    }

    @Override
    public String actorTeamId() {
        return "SYSTEM";
    }

    @Override
    public String unitId() {
        return "simulation";
    }
}
