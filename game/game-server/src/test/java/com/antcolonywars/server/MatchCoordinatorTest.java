package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.AttackCommand;
import com.antcolonywars.core.DefaultMatchSetup;
import com.antcolonywars.core.Direction;
import com.antcolonywars.core.ErrorCode;
import com.antcolonywars.core.Headquarters;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.Position;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchCoordinatorTest {
    @Test
    void sharesTeamRateLimitAcrossCommandsAndReleasesItAfterOneSecond() {
        MutableClock clock = new MutableClock();
        MatchCoordinator coordinator = new MatchCoordinator(
                DefaultMatchSetup.create(), new GameProfileProperties.Limits(2, 4, 250), clock);

        assertTrue(coordinator.apply(new WaitCommand("wait-1", "BLUE", "worker-blue")).accepted());
        assertTrue(coordinator.apply(new WaitCommand("wait-2", "BLUE", "worker-blue")).accepted());
        ApplyResult limited = coordinator.apply(new WaitCommand("wait-3", "BLUE", "worker-blue"));
        assertEquals(ErrorCode.RATE_LIMITED, limited.error());

        clock.advanceMillis(1000);
        assertTrue(coordinator.apply(new WaitCommand("wait-4", "BLUE", "worker-blue")).accepted());
        assertEquals(4, coordinator.commandQueueCapacity());
    }

    @Test
    void simulationTickerAdvancesProjectilesOutsideTheTeamRateLimit() {
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
                new AttackCommand("attack-1", "BLUE", "worker-blue", "worker-red")
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
