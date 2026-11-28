package com.antcolonywars.server;

import com.antcolonywars.core.AttackCommand;
import com.antcolonywars.core.DefaultMatchSetup;
import com.antcolonywars.core.Direction;
import com.antcolonywars.core.Headquarters;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.Position;
import com.antcolonywars.core.QueueTickResult;
import com.antcolonywars.core.RuleSet;
import com.antcolonywars.core.Team;
import com.antcolonywars.core.Unit;
import com.antcolonywars.core.WaitCommand;
import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.MatchState;
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
    private static final GameProfileProperties.Limits LIMITS = new GameProfileProperties.Limits(250);

    @Test
    void persistenceFailureKeepsStateAndQueueUnchanged() {
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
                DefaultMatchSetup.create(), null, LIMITS, Clock.systemUTC(), failing
        );
        coordinator.enqueue("BLUE", "batch", List.of(new WaitCommand("persist-failure", "BLUE", "worker-blue")));

        assertThrows(IllegalStateException.class, coordinator::advanceTick);
        assertEquals(0, coordinator.state().revision());
        assertEquals(1, coordinator.pendingCommands("BLUE").get("worker-blue").size());
    }

    @Test
    void queuesWithoutChangingStateAndRunsOneCommandPerTick() {
        MatchCoordinator coordinator = new MatchCoordinator(DefaultMatchSetup.create(), LIMITS, Clock.systemUTC());

        MatchCoordinator.EnqueueResult queued = coordinator.enqueue("BLUE", "batch", List.of(
                new WaitCommand("wait-1", "BLUE", "worker-blue"),
                new WaitCommand("wait-2", "BLUE", "worker-blue")
        ));

        assertEquals(2, queued.queuedCount());
        assertEquals(0, coordinator.state().revision());
        coordinator.advanceTick();
        assertEquals(1, coordinator.state().revision());
        coordinator.advanceTick();
        assertEquals(2, coordinator.state().revision());
        assertTrue(coordinator.pendingCommands("BLUE").isEmpty());
    }

    @Test
    void refusesABotsShareWhenItsQueueWouldOverflow() {
        MatchCoordinator coordinator = new MatchCoordinator(DefaultMatchSetup.create(), LIMITS, Clock.systemUTC());
        List<WaitCommand> tooMany = java.util.stream.IntStream.rangeClosed(1, 41)
                .mapToObj(index -> new WaitCommand("wait-" + index, "BLUE", "worker-blue"))
                .toList();

        MatchCoordinator.EnqueueResult result = coordinator.enqueue("BLUE", "flood", tooMany);

        assertEquals(0, result.queuedCount());
        assertEquals(List.of("worker-blue"), result.refusedSubjects());
        assertTrue(coordinator.pendingCommands("BLUE").isEmpty());
    }

    @Test
    void clearsOnlyTheRequestingTeamsQueue() {
        MatchCoordinator coordinator = new MatchCoordinator(DefaultMatchSetup.create(), LIMITS, Clock.systemUTC());
        coordinator.enqueue("BLUE", "blue", List.of(new WaitCommand("blue-wait", "BLUE", "worker-blue")));
        coordinator.enqueue("RED", "red", List.of(new WaitCommand("red-wait", "RED", "worker-red")));

        assertEquals(0, coordinator.clearQueue("RED", "worker-blue"));
        assertEquals(1, coordinator.clearQueue("BLUE", "worker-blue"));
        assertTrue(coordinator.pendingCommands("BLUE").isEmpty());
        assertEquals(1, coordinator.pendingCommands("RED").get("worker-red").size());
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
        MatchCoordinator coordinator = new MatchCoordinator(setup, LIMITS, Clock.systemUTC());
        GameWebSocketHandler handler = new GameWebSocketHandler(coordinator, new ObjectMapper());
        SimulationTicker ticker = new SimulationTicker(coordinator, handler);

        coordinator.enqueue("BLUE", "attack", List.of(
                new AttackCommand("attack-1", "BLUE", "worker-blue", Direction.RIGHT)
        ));
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
        MatchCoordinator first = new MatchCoordinator(setup, null, LIMITS, Clock.systemUTC(), persistence);
        first.enqueue("BLUE", "before", List.of(new WaitCommand("wait-before-restart", "BLUE", "worker-blue")));
        first.advanceTick();

        MatchCoordinator restored = new MatchCoordinator(
                setup,
                persistence.load(setup.matchId()).orElseThrow(),
                LIMITS,
                Clock.systemUTC(),
                persistence
        );
        restored.enqueue("BLUE", "after", List.of(new WaitCommand("wait-after-restart", "BLUE", "worker-blue")));
        QueueTickResult next = restored.advanceTick();

        assertTrue(next.rejections().isEmpty());
        assertEquals(2, next.state().revision());
        assertEquals(2, persistence.history(setup.matchId()).size());
    }

    @Test
    void advancesConfiguredMatchesAndRecordsTickLatencyUnderLoad() {
        MatchRegistry registry = new MatchRegistry(
                DefaultMatchSetup.create(),
                List.of("load-one", "load-two"),
                LIMITS,
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

    @Test
    void snapshotSavedInAnOlderFormatIsDiscardedInsteadOfStoppingTheServer() {
        MatchPersistence unreadable = new MatchPersistence() {
            @Override public Optional<MatchState> load(String matchId) {
                throw new IllegalStateException("Cannot deserialize persisted match data");
            }
            @Override public void save(MatchState state, GameEvent event) { }
            @Override public List<GameEvent> history(String matchId) { return List.of(); }
        };

        MatchRegistry registry = new MatchRegistry(
                DefaultMatchSetup.create(), List.of("old-format"), LIMITS, Clock.systemUTC(), unreadable
        );

        assertEquals(0, registry.require("old-format").state().revision());
    }

    @Test
    void stateSentToViewersCarriesMaxHpOfDamagedEntities() throws Exception {
        Unit damaged = new Unit("u", "U", "BLUE", "WORKER", new Position(1, 1), 3, Direction.UP, 1, 1).takeDamage(2);
        String json = new ObjectMapper().writeValueAsString(damaged);

        assertTrue(json.contains("\"hp\":1"), json);
        assertTrue(json.contains("\"maxHp\":3"), json);
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
