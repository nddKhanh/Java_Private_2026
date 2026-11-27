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

    /**
     * Runs one tick: the head of every queue in queued order, then projectiles and towers.
     * A head whose subject does not exist yet waits while an earlier command of its batch is still queued
     * (for example a SPAWN from a barracks the same batch is building); any other failure drops the rest
     * of that batch for that subject. Once the match is finished every queue is emptied.
     */
    public QueueTickResult advanceTick(CommandQueue queue) {
        List<GameEvent> events = new ArrayList<>();
        List<CommandRejection> rejections = new ArrayList<>();
        for (CommandQueue.Entry head : queue.heads()) {
            ApplyResult result = apply(head.command());
            if (result.accepted()) {
                queue.pop(head);
                events.addAll(result.events());
                continue;
            }
            if (waitsForEarlierCommand(result.error()) && queue.hasEarlierPending(head)) {
                continue;
            }
            int dropped = queue.dropBatch(head);
            rejections.add(new CommandRejection(
                    head.teamId(), head.batchId(), head.command().commandId(), head.subjectId(),
                    CommandRejection.actionOf(head.command()), result.error(), Math.max(0, dropped - 1)
            ));
        }
        events.addAll(advanceTick().events());
        if (state.status() == MatchStatus.FINISHED) {
            queue.clearAll();
        }
        return new QueueTickResult(state, events, rejections);
    }

    private static boolean waitsForEarlierCommand(ErrorCode error) {
        return error == ErrorCode.UNIT_NOT_FOUND || error == ErrorCode.PRODUCTION_SOURCE_NOT_FOUND;
    }

    public ApplyResult advanceTick() {
        if (!engine.hasSimulationWork(state, rules)) {
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
