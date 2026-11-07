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
    private final Map<String, Deque<Long>> actionTimes = new HashMap<>();
    private final Deque<GameCommand> commandQueue = new ArrayDeque<>();

    public MatchCoordinator(MatchSetup setup) {
        this(setup, new GameProfileProperties.Limits(10, 32));
    }

    public MatchCoordinator(MatchSetup setup, GameProfileProperties.Limits limits) {
        this(setup, limits, Clock.systemUTC());
    }

    MatchCoordinator(MatchSetup setup, GameProfileProperties.Limits limits, Clock clock) {
        session = new GameSession(setup);
        actionsPerSecond = limits.actionsPerSecond();
        commandQueueCapacity = limits.commandQueueCapacity();
        this.clock = clock;
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
            return session.apply(commandQueue.removeFirst());
        } finally {
            commandQueue.remove(command);
        }
    }

    public synchronized MatchState state() {
        return session.state();
    }

    int commandQueueCapacity() {
        return commandQueueCapacity;
    }
}
