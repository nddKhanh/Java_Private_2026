package com.antcolonywars.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MatchFactory {
    private MatchFactory() {
    }

    public static MatchState create(MatchSetup setup) {
        validate(setup);
        return new MatchState(
                setup.matchId(),
                setup.setupId(),
                setup.rules().version(),
                MatchStatus.RUNNING,
                null,
                0,
                0,
                setup.mapWidth(),
                setup.mapHeight(),
                setup.teams(),
                setup.units(),
                setup.headquarters(),
                List.of()
        );
    }

    private static void validate(MatchSetup setup) {
        Set<String> teamIds = new HashSet<>();
        for (Team team : setup.teams()) {
            requireUnique(team.id(), teamIds, "team");
        }

        Set<String> entityIds = new HashSet<>();
        Set<Position> occupied = new HashSet<>();
        for (Unit unit : setup.units()) {
            validateEntity(unit.id(), unit.teamId(), unit.position(), unit.hp(), setup, teamIds, entityIds, occupied);
        }
        for (Headquarters headquarters : setup.headquarters()) {
            validateEntity(headquarters.id(), headquarters.teamId(), headquarters.position(), headquarters.hp(),
                    setup, teamIds, entityIds, occupied);
        }
    }

    private static void validateEntity(
            String id,
            String teamId,
            Position position,
            int hp,
            MatchSetup setup,
            Set<String> teamIds,
            Set<String> entityIds,
            Set<Position> occupied
    ) {
        requireUnique(id, entityIds, "entity");
        if (!teamIds.contains(teamId)) {
            throw new IllegalArgumentException("Unknown team for entity: " + id);
        }
        if (hp <= 0) {
            throw new IllegalArgumentException("Seed entity hp must be positive: " + id);
        }
        if (!withinBounds(position, setup.mapWidth(), setup.mapHeight())) {
            throw new IllegalArgumentException("Entity is outside map: " + id);
        }
        if (!occupied.add(position)) {
            throw new IllegalArgumentException("Multiple entities occupy " + position);
        }
    }

    private static void requireUnique(String id, Set<String> ids, String kind) {
        if (!ids.add(id)) {
            throw new IllegalArgumentException("Duplicate " + kind + " id: " + id);
        }
    }

    private static boolean withinBounds(Position position, int width, int height) {
        return position.x() >= 0 && position.x() < width && position.y() >= 0 && position.y() < height;
    }
}
