package com.antcolonywars.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class GameSession {
    private final RuleSet rules;
    private final GameEngine engine;
    private final List<ActionCommand> commandLog = new ArrayList<>();
    private MatchState state;

    public GameSession(MatchSetup setup) {
        Objects.requireNonNull(setup, "setup");
        this.rules = setup.rules();
        this.engine = new GameEngine();
        this.state = MatchFactory.create(setup);
    }

    public ApplyResult apply(GameCommand command) {
        ApplyResult result = engine.apply(state, command, rules);
        if (result.accepted() && command instanceof ActionCommand action) {
            state = result.state();
            commandLog.add(action);
        }
        return result;
    }

    public MatchState state() {
        return state;
    }

    public List<ActionCommand> commandLog() {
        return List.copyOf(commandLog);
    }
}
