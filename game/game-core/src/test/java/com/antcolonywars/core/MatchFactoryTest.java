package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MatchFactoryTest {
    @Test
    void rejectsOverlappingSeedEntities() {
        MatchSetup setup = new MatchSetup(
                "invalid",
                "invalid-match",
                3,
                3,
                RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue")),
                List.of(new Unit("worker-blue", "BLUE", "WORKER", new Position(0, 0), 3, Direction.RIGHT)),
                List.of(new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3))
        );

        assertThrows(IllegalArgumentException.class, () -> MatchFactory.create(setup));
    }
}
