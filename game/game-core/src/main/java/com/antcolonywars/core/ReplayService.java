package com.antcolonywars.core;

import java.util.List;
import java.util.Objects;

public final class ReplayService {
    public MatchState replay(MatchSetup setup, List<? extends ActionCommand> commands) {
        Objects.requireNonNull(commands, "commands");
        GameSession session = new GameSession(setup);
        for (ActionCommand command : commands) {
            ApplyResult result = session.apply(command);
            if (!result.accepted()) {
                throw new IllegalArgumentException(
                        "Command log contains rejected command " + command.commandId() + ": " + result.error()
                );
            }
        }
        return session.state();
    }
}
