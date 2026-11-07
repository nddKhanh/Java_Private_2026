package com.antcolonywars.core;

import java.util.List;
import java.util.Objects;

public record MatchSetup(
        String setupId,
        String matchId,
        int mapWidth,
        int mapHeight,
        RuleSet rules,
        List<Team> teams,
        List<Unit> units,
        List<Headquarters> headquarters
) {
    public MatchSetup {
        Objects.requireNonNull(setupId, "setupId");
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(rules, "rules");
        teams = List.copyOf(teams);
        units = List.copyOf(units);
        headquarters = List.copyOf(headquarters);
        if (mapWidth <= 0 || mapHeight <= 0) {
            throw new IllegalArgumentException("Map dimensions must be positive");
        }
    }
}
