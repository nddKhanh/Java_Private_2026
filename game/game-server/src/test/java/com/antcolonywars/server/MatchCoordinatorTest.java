package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.AttackCommand;
import com.antcolonywars.core.BatchApplyResult;
import com.antcolonywars.core.BatchMode;
import com.antcolonywars.core.Building;
import com.antcolonywars.core.BuildingType;
import com.antcolonywars.core.DefaultMatchSetup;
import com.antcolonywars.core.Direction;
import com.antcolonywars.core.ErrorCode;
import com.antcolonywars.core.Headquarters;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.Position;
import com.antcolonywars.core.RuleSet;
import com.antcolonywars.core.SpawnCommand;
import com.antcolonywars.core.Team;
import com.antcolonywars.core.Unit;
import com.antcolonywars.core.WaitCommand;
import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.MatchState;
import com.antcolonywars.core.MoveCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchCoordinatorTest {
    @Test
    void persistenceFailureDoesNotPublishCandidateState() {
        MatchPersistence failing = new MatchPersistence() {
            private MatchState stored;
            @Override public Optional<MatchState> load(String matchId) { return Optional.ofNullable(stored); }
            @Override public void save(MatchState state, GameEvent event) {
                if (event != null) throw new IllegalStateException("injected database failure");
                stored = state;
            }
            @Override public void saveBatch(MatchState state, List<GameEvent> events) {
                throw new IllegalStateException("injected database failure");
            }
            @Override public List<GameEvent> history(String matchId) { return List.of(); }
        };
        MatchCoordinator coordinator = new MatchCoordinator(
                DefaultMatchSetup.create(), null,
                new GameProfileProperties.Limits(10, 32, 250), Clock.systemUTC(), failing
        );
        assertThrows(IllegalStateException.class, () -> coordinator.applyBatch(List.of(
                new WaitCommand("persist-failure", "BLUE", "worker-blue")
        ), BatchMode.ATOMIC));
        assertEquals(0, coordinator.state().revision());
    }

    @Test
    void limitsActionsPerBotInEachBatchWithoutRateLimitingSeparateBatches() {
        MatchCoordinator coordinator = new MatchCoordinator(
                DefaultMatchSetup.create(), new GameProfileProperties.Limits(2, 4, 250), Clock.systemUTC());

        BatchApplyResult atomic = coordinator.applyBatch(List.of(
                new WaitCommand("atomic-1", "BLUE", "worker-blue"),
                new WaitCommand("atomic-2", "BLUE", "worker-blue"),
                new WaitCommand("atomic-3", "BLUE", "worker-blue")
        ), BatchMode.ATOMIC);
        assertEquals(ErrorCode.BATCH_LIMIT_EXCEEDED, atomic.error());
        assertEquals(0, atomic.state().revision());

        BatchApplyResult partial = coordinator.applyBatch(List.of(
                new WaitCommand("partial-1", "BLUE", "worker-blue"),
                new WaitCommand("partial-2", "BLUE", "worker-blue"),
                new WaitCommand("partial-3", "BLUE", "worker-blue")
        ), BatchMode.PARTIAL);
        assertEquals(ErrorCode.BATCH_LIMIT_EXCEEDED, partial.error());
        assertEquals(2, partial.appliedCount());
        assertEquals(2, partial.state().revision());

        assertTrue(coordinator.apply(new WaitCommand("next-1", "BLUE", "worker-blue")).accepted());
        assertTrue(coordinator.apply(new WaitCommand("next-2", "BLUE", "worker-blue")).accepted());
        assertTrue(coordinator.apply(new WaitCommand("next-3", "BLUE", "worker-blue")).accepted());
        assertEquals(5, coordinator.state().revision());
        assertEquals(4, coordinator.commandQueueCapacity());
    }

    @Test
    void countsSpawnAgainstItsSelectedBuildingWithoutMixingOtherSubjects() {
        MatchSetup setup = new MatchSetup(
                "building-limit", "building-limit", 7, 5, RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue"), new Team("RED", "Red", "red")),
                List.of(new Unit("worker-blue", "BLUE", "WORKER", new Position(5, 4), 3, Direction.LEFT)),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(6, 4), 3)
                ),
                List.of(),
                List.of(
                        new Building("barracks-a", "BLUE", BuildingType.BARRACKS, new Position(1, 1), 5),
                        new Building("barracks-b", "BLUE", BuildingType.BARRACKS, new Position(4, 1), 5)
                )
        );
        GameProfileProperties.Limits limits = new GameProfileProperties.Limits(2, 16, 250);
        MatchCoordinator limited = new MatchCoordinator(setup, limits, Clock.systemUTC());

        BatchApplyResult partial = limited.applyBatch(List.of(
                spawn("spawn-a-1", "barracks-a", "melee-a-1", Direction.RIGHT),
                spawn("spawn-a-2", "barracks-a", "melee-a-2", Direction.DOWN),
                spawn("spawn-a-3", "barracks-a", "melee-a-3", Direction.UP)
        ), BatchMode.PARTIAL);

        assertEquals(ErrorCode.BATCH_LIMIT_EXCEEDED, partial.error());
        assertEquals(2, partial.failedIndex());
        assertEquals(2, partial.appliedCount());

        MatchCoordinator independent = new MatchCoordinator(setup, limits, Clock.systemUTC());
        BatchApplyResult mixed = independent.applyBatch(List.of(
                new WaitCommand("worker-1", "BLUE", "worker-blue"),
                new WaitCommand("worker-2", "BLUE", "worker-blue"),
                spawn("spawn-a", "barracks-a", "melee-a", Direction.RIGHT),
                spawn("spawn-b", "barracks-b", "melee-b", Direction.LEFT)
        ), BatchMode.ATOMIC);

        assertTrue(mixed.accepted());
        assertEquals(4, mixed.appliedCount());
    }

    @Test
    void rejectsSpawnWithoutSourceBeforeDifferentUnitIdsCanBypassTheLimit() {
        MatchCoordinator coordinator = new MatchCoordinator(
                DefaultMatchSetup.create(), new GameProfileProperties.Limits(10, 32, 250), Clock.systemUTC()
        );
        List<SpawnCommand> commands = java.util.stream.IntStream.rangeClosed(1, 11)
                .mapToObj(index -> new SpawnCommand(
                        "missing-source-" + index, "BLUE", "worker-new-" + index,
                        "Worker " + index, "WORKER", Direction.DOWN
                ))
                .toList();

        BatchApplyResult atomic = coordinator.applyBatch(commands, BatchMode.ATOMIC);
        BatchApplyResult partial = coordinator.applyBatch(commands, BatchMode.PARTIAL);

        assertEquals(ErrorCode.PRODUCTION_SOURCE_NOT_FOUND, atomic.error());
        assertEquals(0, atomic.failedIndex());
        assertEquals(0, atomic.appliedCount());
        assertEquals(ErrorCode.PRODUCTION_SOURCE_NOT_FOUND, partial.error());
        assertEquals(0, partial.failedIndex());
        assertEquals(0, partial.appliedCount());
    }

    @Test
    void reportsTheFirstGameRuleFailureBeforeALaterBatchLimit() {
        List<com.antcolonywars.core.ActionCommand> commands = new java.util.ArrayList<>();
        commands.add(new MoveCommand("outside", "BLUE", "worker-blue", Direction.UP));
        for (int index = 1; index <= 10; index++) {
            commands.add(new WaitCommand("wait-" + index, "BLUE", "worker-blue"));
        }
        GameProfileProperties.Limits limits = new GameProfileProperties.Limits(10, 32, 250);

        BatchApplyResult atomic = new MatchCoordinator(
                DefaultMatchSetup.create(), limits, Clock.systemUTC()
        ).applyBatch(commands, BatchMode.ATOMIC);
        BatchApplyResult partial = new MatchCoordinator(
                DefaultMatchSetup.create(), limits, Clock.systemUTC()
        ).applyBatch(commands, BatchMode.PARTIAL);

        assertEquals(ErrorCode.OUT_OF_BOUNDS, atomic.error());
        assertEquals(0, atomic.failedIndex());
        assertEquals(0, atomic.appliedCount());
        assertEquals(ErrorCode.OUT_OF_BOUNDS, partial.error());
        assertEquals(0, partial.failedIndex());
        assertEquals(0, partial.appliedCount());
    }

    private SpawnCommand spawn(String commandId, String sourceId, String unitId, Direction direction) {
        return new SpawnCommand(commandId, "BLUE", sourceId, unitId, unitId, "MELEE", direction);
    }

    @Test
    void simulationTickerAdvancesProjectilesOutsidePlayerBatches() {
        MatchSetup setup = new MatchSetup(
                "tick-setup", "tick-match", 5, 5, RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue"), new Team("RED", "Red", "red")),
                List.of(
                        new Unit("worker-blue", "BLUE", "WORKER", new Position(1, 1), 3, Direction.RIGHT),
                        new Unit("worker-red", "RED", "WORKER", new Position(2, 1), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(4, 4), 3)
                )
        );
        MatchCoordinator coordinator = new MatchCoordinator(
                setup, new GameProfileProperties.Limits(1, 4, 250), Clock.systemUTC()
        );
        GameWebSocketHandler handler = new GameWebSocketHandler(coordinator, new ObjectMapper());
        SimulationTicker ticker = new SimulationTicker(coordinator, handler);

        assertTrue(coordinator.apply(
                new AttackCommand("attack-1", "BLUE", "worker-blue", Direction.RIGHT)
        ).accepted());
        ticker.tick();

        assertEquals(1, coordinator.state().simulationTick());
        assertEquals(2, coordinator.state().units().stream()
                .filter(unit -> unit.id().equals("worker-red"))
                .findFirst().orElseThrow().hp());
    }

    @Test
    void restoresThePersistedSnapshotAndContinuesItsRevision() {
        MatchSetup setup = DefaultMatchSetup.create();
        RecordingPersistence persistence = new RecordingPersistence();
        MatchCoordinator first = new MatchCoordinator(
                setup, null, new GameProfileProperties.Limits(10, 32, 250), Clock.systemUTC(), persistence
        );
        assertTrue(first.apply(new WaitCommand("wait-before-restart", "BLUE", "worker-blue")).accepted());

        MatchCoordinator restored = new MatchCoordinator(
                setup,
                persistence.load(setup.matchId()).orElseThrow(),
                new GameProfileProperties.Limits(10, 32, 250),
                Clock.systemUTC(),
                persistence
        );
        ApplyResult next = restored.apply(new WaitCommand("wait-after-restart", "BLUE", "worker-blue"));

        assertTrue(next.accepted());
        assertEquals(2, next.state().revision());
        assertEquals(2, persistence.history(setup.matchId()).size());
    }

    @Test
    void advancesConfiguredMatchesAndRecordsTickLatencyUnderLoad() {
        MatchRegistry registry = new MatchRegistry(
                DefaultMatchSetup.create(),
                List.of("load-one", "load-two"),
                new GameProfileProperties.Limits(10, 32, 250),
                Clock.systemUTC(),
                MatchPersistence.none()
        );

        long started = System.nanoTime();
        for (int sample = 0; sample < 200; sample++) {
            registry.advanceTicks();
        }
        long elapsedMillis = (System.nanoTime() - started) / 1_000_000;

        assertEquals(2, registry.runtimes().size());
        assertTrue(registry.runtimes().stream().allMatch(runtime -> runtime.tickSamples() == 200));
        assertTrue(elapsedMillis < 5_000, "400 match ticks took " + elapsedMillis + " ms");
    }

    private static final class MutableClock extends Clock {
        private long millis;

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(millis);
        }

        void advanceMillis(long amount) {
            millis += amount;
        }
    }

    private static final class RecordingPersistence implements MatchPersistence {
        private MatchState state;
        private final java.util.ArrayList<GameEvent> events = new java.util.ArrayList<>();

        @Override
        public Optional<MatchState> load(String matchId) {
            return Optional.ofNullable(state).filter(candidate -> candidate.matchId().equals(matchId));
        }

        @Override
        public void save(MatchState state, GameEvent event) {
            this.state = state;
            if (event != null) {
                events.add(event);
            }
        }

        @Override
        public List<GameEvent> history(String matchId) {
            return events.stream().filter(event -> state.matchId().equals(matchId)).toList();
        }
    }
}
