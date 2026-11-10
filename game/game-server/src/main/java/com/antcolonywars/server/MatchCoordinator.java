package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.GameCommand;
import com.antcolonywars.core.GameSession;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.MatchState;

import java.time.Clock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class MatchCoordinator {
    private final GameSession session;
    private final int actionsPerSecond;
    private final int commandQueueCapacity;
    private final Clock clock;
    private final MatchPersistence persistence;
    private final Map<String, Deque<Long>> actionTimes = new HashMap<>();
    private final Deque<GameCommand> commandQueue = new ArrayDeque<>();

    public MatchCoordinator(MatchSetup setup) {
        this(setup, new GameProfileProperties.Limits(10, 32, 250), Clock.systemUTC());
    }

    public MatchCoordinator(MatchSetup setup, GameProfileProperties.Limits limits) {
        this(setup, limits, Clock.systemUTC());
    }

    MatchCoordinator(MatchSetup setup, GameProfileProperties.Limits limits, Clock clock) {
        this(setup, null, limits, clock, MatchPersistence.none());
    }

    MatchCoordinator(
            MatchSetup setup,
            MatchState restoredState,
            GameProfileProperties.Limits limits,
            Clock clock,
            MatchPersistence persistence
    ) {
        session = new GameSession(setup, restoredState);
        actionsPerSecond = limits.actionsPerSecond();
        commandQueueCapacity = limits.commandQueueCapacity();
        this.clock = clock;
        this.persistence = persistence;
        persistence.save(session.state(), null);
    }

    public synchronized ApplyResult apply(GameCommand command) {
        if (commandQueue.size() >= commandQueueCapacity) {
            return ApplyResult.failure(session.state(), com.antcolonywars.core.ErrorCode.COMMAND_QUEUE_FULL);
        }
        String teamId = command instanceof com.antcolonywars.core.ActionCommand action
                ? action.actorTeamId() : "anonymous";
        long now = clock.millis();
        Deque<Long> times = actionTimes.computeIfAbsent(teamId, ignored -> new ArrayDeque<>());
        while (!times.isEmpty() && now - times.peekFirst() >= 1000) {
            times.removeFirst();
        }
        if (times.size() >= actionsPerSecond) {
            return ApplyResult.failure(session.state(), com.antcolonywars.core.ErrorCode.RATE_LIMITED);
        }
        times.addLast(now);
        commandQueue.addLast(command);
        try {
            ApplyResult result = session.apply(commandQueue.removeFirst());
            persist(result);
            return result;
        } finally {
            commandQueue.remove(command);
        }
    }

    public synchronized MatchState state() {
        return session.state();
    }

    public synchronized ApplyResult advanceTick() {
        ApplyResult result = session.advanceTick();
        persist(result);
        return result;
    }

    int commandQueueCapacity() {
        return commandQueueCapacity;
    }

    private void persist(ApplyResult result) {
        if (result.accepted() && !result.events().isEmpty()) {
            persistence.save(result.state(), result.events().getFirst());
        }
    }
}
