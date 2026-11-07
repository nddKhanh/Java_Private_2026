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
        List<Headquarters> headquarters,
        List<ResourceNode> resourceNodes,
        List<Building> buildings
) {
    public MatchSetup {
        Objects.requireNonNull(setupId, "setupId");
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(rules, "rules");
        teams = List.copyOf(teams);
        units = List.copyOf(units);
        headquarters = List.copyOf(headquarters);
        resourceNodes = List.copyOf(resourceNodes);
        buildings = List.copyOf(buildings);
        if (mapWidth <= 0 || mapHeight <= 0) {
            throw new IllegalArgumentException("Map dimensions must be positive");
        }
    }

    public MatchSetup(
            String setupId,
            String matchId,
            int mapWidth,
            int mapHeight,
            RuleSet rules,
            List<Team> teams,
            List<Unit> units,
            List<Headquarters> headquarters
    ) {
        this(setupId, matchId, mapWidth, mapHeight, rules, teams, units, headquarters, List.of(), List.of());
    }
}
