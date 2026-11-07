package com.antcolonywars.core;

import java.util.List;
import java.util.Objects;

public record MatchState(
        String matchId,
        String setupId,
        String rulesVersion,
        MatchStatus status,
        String winnerTeamId,
        long revision,
        long eventSequence,
        int mapWidth,
        int mapHeight,
        List<Team> teams,
        List<Unit> units,
        List<String> retiredUnitIds,
        List<Headquarters> headquarters,
        List<ResourceNode> resourceNodes,
        List<Building> buildings,
        List<String> processedCommandIds
) {
    public MatchState {
        Objects.requireNonNull(matchId, "matchId");
        Objects.requireNonNull(setupId, "setupId");
        Objects.requireNonNull(rulesVersion, "rulesVersion");
        Objects.requireNonNull(status, "status");
        teams = List.copyOf(teams);
        units = List.copyOf(units);
        retiredUnitIds = List.copyOf(retiredUnitIds);
        headquarters = List.copyOf(headquarters);
        resourceNodes = List.copyOf(resourceNodes);
        buildings = List.copyOf(buildings);
        processedCommandIds = List.copyOf(processedCommandIds);
    }
}
