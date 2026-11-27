package com.antcolonywars.server;

import com.antcolonywars.core.ActionCommand;
import com.antcolonywars.core.CommandQueue;
import com.antcolonywars.core.GameSession;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.MatchState;
import com.antcolonywars.core.MatchStatus;
import com.antcolonywars.core.QueueTickResult;

import java.time.Clock;
import java.util.List;
import java.util.Map;

/** Owns one match: its state, the command queue of every bot and the persistence of each tick. */
public final class MatchCoordinator {
    private GameSession session;
    private CommandQueue queue;
    private final MatchSetup setup;
    private final MatchPersistence persistence;

    public MatchCoordinator(MatchSetup setup) {
        this(setup, new GameProfileProperties.Limits(250), Clock.systemUTC());
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
        queue = new CommandQueue(setup.rules().commands().maxPerBot());
        this.persistence = persistence;
        persistence.save(session.state(), null);
    }

    /** Appends a team's batch to the queues of its bots; returns the subjects whose share was refused. */
    public synchronized EnqueueResult enqueue(String teamId, String batchId, List<? extends ActionCommand> commands) {
        if (session.state().status() == MatchStatus.FINISHED) {
            return new EnqueueResult(0, commands.stream().map(CommandQueue::subjectOf).distinct().toList(), true);
        }
        List<String> refused = queue.enqueue(batchId, teamId, commands);
        long queued = commands.stream().filter(command -> !refused.contains(CommandQueue.subjectOf(command))).count();
        return new EnqueueResult((int) queued, refused, false);
    }

    public synchronized int clearQueue(String teamId, String subjectId) {
        return queue.clear(teamId, subjectId);
    }

    public synchronized Map<String, List<ActionCommand>> pendingCommands(String teamId) {
        return queue.pendingFor(teamId);
    }

    public synchronized MatchState state() {
        return session.state();
    }

    /** Runs one tick on copies and keeps them only after the tick's events are persisted. */
    public synchronized QueueTickResult advanceTick() {
        GameSession candidate = new GameSession(setup, session.state());
        CommandQueue candidateQueue = queue.copy();
        QueueTickResult result = candidate.advanceTick(candidateQueue);
        if (!result.events().isEmpty()) {
            persistence.saveBatch(result.state(), result.events());
        }
        session = candidate;
        queue = candidateQueue;
        return result;
    }

    public int maxBatchCommands() {
        return setup.rules().commands().maxPerBatch();
    }

    public record EnqueueResult(int queuedCount, List<String> refusedSubjects, boolean matchFinished) {
        public EnqueueResult {
            refusedSubjects = List.copyOf(refusedSubjects);
        }
    }
}
