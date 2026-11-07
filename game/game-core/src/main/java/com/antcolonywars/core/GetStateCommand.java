package com.antcolonywars.core;

import java.util.Objects;

public record GetStateCommand(String commandId) implements GameCommand {
    public GetStateCommand {
        Objects.requireNonNull(commandId, "commandId");
    }
}
