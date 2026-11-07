package com.antcolonywars.core;

import java.util.List;

public final class DefaultMatchSetup {
    private DefaultMatchSetup() {
    }

    public static MatchSetup create() {
        return new MatchSetup(
                "mvp-default",
                "match-default",
                8,
                8,
                RuleSet.mvp(),
                List.of(
                        new Team("BLUE", "Blue", "#2563eb"),
                        new Team("RED", "Red", "#dc2626")
                ),
                List.of(
                        new Unit("worker-blue", "BLUE", "WORKER", new Position(1, 0), 3, Direction.RIGHT),
                        new Unit("worker-red", "RED", "WORKER", new Position(6, 7), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(7, 7), 3)
                )
        );
    }
}
