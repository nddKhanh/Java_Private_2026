package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.ActionCommand;
import com.antcolonywars.core.BatchApplyResult;
import com.antcolonywars.core.BatchMode;
import com.antcolonywars.core.ErrorCode;
import com.antcolonywars.core.GameCommand;
import com.antcolonywars.core.GameSession;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.MatchState;
import com.antcolonywars.core.SpawnCommand;

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MatchCoordinator {
    private GameSession session;
    private final MatchSetup setup;
    private final int maxActionsPerBotPerBatch;
    private final int commandQueueCapacity;
    private final MatchPersistence persistence;

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
        this.setup = setup;
        session = new GameSession(setup, restoredState);
        maxActionsPerBotPerBatch = limits.maxActionsPerBotPerBatch();
        commandQueueCapacity = limits.commandQueueCapacity();
        this.persistence = persistence;
        persistence.save(session.state(), null);
    }

    public synchronized ApplyResult apply(GameCommand command) {
        if (!(command instanceof ActionCommand action)) {
            return session.apply(command);
        }
        BatchApplyResult batch = applyBatch(List.of(action), BatchMode.ATOMIC);
        if (!batch.accepted()) {
            return ApplyResult.failure(batch.state(), batch.error());
        }
        return ApplyResult.success(batch.state(), batch.events());
    }

    public synchronized BatchApplyResult applyBatch(List<? extends ActionCommand> commands, BatchMode mode) {
        int limitIndex = firstLimitViolation(commands);
        GameSession candidate = new GameSession(setup, session.state());
        BatchApplyResult result = candidate.applyBatch(
                commands, mode, limitIndex, ErrorCode.BATCH_LIMIT_EXCEEDED
        );
        persist(result);
        if (result.accepted() || result.appliedCount() > 0) session = candidate;
        return result;
    }

    public synchronized MatchState state() {
        return session.state();
    }

    public synchronized ApplyResult advanceTick() {
        GameSession candidate = new GameSession(setup, session.state());
        ApplyResult result = candidate.advanceTick();
        if (!result.events().isEmpty()) {
            persistence.saveBatch(result.state(), result.events());
            session = candidate;
        }
        return result;
    }

    int commandQueueCapacity() {
        return commandQueueCapacity;
    }

    private int firstLimitViolation(List<? extends ActionCommand> commands) {
        Map<String, Integer> actionsByBot = new HashMap<>();
        for (int index = 0; index < commands.size(); index++) {
            ActionCommand command = commands.get(index);
            String botId = command instanceof SpawnCommand spawn
                    ? spawn.sourceId()
                    : command.unitId();
            int count = actionsByBot.merge(botId, 1, Integer::sum);
            if (count > maxActionsPerBotPerBatch) {
                return index;
            }
        }
        return -1;
    }

    private void persist(BatchApplyResult result) {
        if (!result.events().isEmpty()) persistence.saveBatch(result.state(), result.events());
    }
}
