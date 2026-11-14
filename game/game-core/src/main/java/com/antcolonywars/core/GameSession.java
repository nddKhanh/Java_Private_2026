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
        this(setup, null);
    }

    public GameSession(MatchSetup setup, MatchState restoredState) {
        Objects.requireNonNull(setup, "setup");
        this.rules = setup.rules();
        this.engine = new GameEngine();
        MatchState initialState = MatchFactory.create(setup);
        if (restoredState != null) {
            validateRestoredState(initialState, restoredState);
        }
        this.state = restoredState == null ? initialState : restoredState;
    }

    public ApplyResult apply(GameCommand command) {
        ApplyResult result = engine.apply(state, command, rules);
        if (result.accepted() && command instanceof ActionCommand action) {
            state = result.state();
            commandLog.add(action);
        }
        return result;
    }

    public BatchApplyResult applyBatch(List<? extends ActionCommand> commands, BatchMode mode) {
        Objects.requireNonNull(commands, "commands");
        Objects.requireNonNull(mode, "mode");
        MatchState originalState = state;
        MatchState workingState = state;
        List<GameEvent> events = new ArrayList<>();
        List<ActionCommand> acceptedCommands = new ArrayList<>();

        for (int index = 0; index < commands.size(); index++) {
            ActionCommand command = Objects.requireNonNull(commands.get(index), "command");
            ApplyResult result = engine.apply(workingState, command, rules);
            if (!result.accepted()) {
                if (mode == BatchMode.ATOMIC) {
                    return BatchApplyResult.failure(originalState, List.of(), result.error(), index, 0);
                }
                state = workingState;
                commandLog.addAll(acceptedCommands);
                return BatchApplyResult.failure(
                        workingState, events, result.error(), index, acceptedCommands.size()
                );
            }
            workingState = result.state();
            events.addAll(result.events());
            acceptedCommands.add(command);
        }

        state = workingState;
        commandLog.addAll(acceptedCommands);
        return BatchApplyResult.success(workingState, events, acceptedCommands.size());
    }

    public ApplyResult advanceTick() {
        if (!engine.hasSimulationWork(state)) {
            return ApplyResult.success(state, List.of());
        }
        return apply(new TickCommand("system:tick:" + (state.simulationTick() + 1)));
    }

    public MatchState state() {
        return state;
    }

    public List<ActionCommand> commandLog() {
        return List.copyOf(commandLog);
    }

    private void validateRestoredState(MatchState initialState, MatchState restoredState) {
        if (!initialState.matchId().equals(restoredState.matchId())
                || !initialState.setupId().equals(restoredState.setupId())
                || !initialState.rulesVersion().equals(restoredState.rulesVersion())
                || initialState.mapWidth() != restoredState.mapWidth()
                || initialState.mapHeight() != restoredState.mapHeight()
                || !initialState.teams().stream().map(Team::id).toList()
                .equals(restoredState.teams().stream().map(Team::id).toList())) {
            throw new IllegalArgumentException("Restored state does not match the configured setup");
        }
    }
}
