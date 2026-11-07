package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.DefaultMatchSetup;
import com.antcolonywars.core.ErrorCode;
import com.antcolonywars.core.WaitCommand;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchCoordinatorTest {
    @Test
    void sharesTeamRateLimitAcrossCommandsAndReleasesItAfterOneSecond() {
        MutableClock clock = new MutableClock();
        MatchCoordinator coordinator = new MatchCoordinator(
                DefaultMatchSetup.create(), new GameProfileProperties.Limits(2, 4), clock);

        assertTrue(coordinator.apply(new WaitCommand("wait-1", "BLUE", "worker-blue")).accepted());
        assertTrue(coordinator.apply(new WaitCommand("wait-2", "BLUE", "worker-blue")).accepted());
        ApplyResult limited = coordinator.apply(new WaitCommand("wait-3", "BLUE", "worker-blue"));
        assertEquals(ErrorCode.RATE_LIMITED, limited.error());

        clock.advanceMillis(1000);
        assertTrue(coordinator.apply(new WaitCommand("wait-4", "BLUE", "worker-blue")).accepted());
        assertEquals(4, coordinator.commandQueueCapacity());
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
}
