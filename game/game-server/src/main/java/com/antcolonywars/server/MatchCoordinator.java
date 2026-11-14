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

import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MatchCoordinator {
    private final GameSession session;
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
        if (limitIndex >= 0 && mode == BatchMode.ATOMIC) {
            return BatchApplyResult.failure(
                    session.state(), List.of(), ErrorCode.BATCH_LIMIT_EXCEEDED, limitIndex, 0
            );
        }

        List<? extends ActionCommand> acceptedRange = limitIndex < 0
                ? commands : commands.subList(0, limitIndex);
        BatchApplyResult result = session.applyBatch(acceptedRange, mode);
        persist(result);
        if (!result.accepted() || limitIndex < 0) {
            return result;
        }
        return BatchApplyResult.failure(
                result.state(), result.events(), ErrorCode.BATCH_LIMIT_EXCEEDED,
                limitIndex, result.appliedCount()
        );
    }

    public synchronized MatchState state() {
        return session.state();
    }

    public synchronized ApplyResult advanceTick() {
        ApplyResult result = session.advanceTick();
        result.events().forEach(event -> persistence.save(result.state(), event));
        return result;
    }

    int commandQueueCapacity() {
        return commandQueueCapacity;
    }

    private int firstLimitViolation(List<? extends ActionCommand> commands) {
        Map<String, Integer> actionsByBot = new HashMap<>();
        for (int index = 0; index < commands.size(); index++) {
            String botId = commands.get(index).unitId();
            int count = actionsByBot.merge(botId, 1, Integer::sum);
            if (count > maxActionsPerBotPerBatch) {
                return index;
            }
        }
        return -1;
    }

    private void persist(BatchApplyResult result) {
        result.events().forEach(event -> persistence.save(result.state(), event));
    }
}
