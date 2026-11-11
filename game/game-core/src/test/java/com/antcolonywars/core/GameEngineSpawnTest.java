package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineSpawnTest {
    @Test
    void spawnsEverySupportedUnitType() {
        for (UnitType type : UnitType.values()) {
            GameSession session = new GameSession(DefaultMatchSetup.create());
            String unitId = "spawned-" + type.name().toLowerCase();

            ApplyResult result = session.apply(new SpawnCommand(
                    "spawn-" + type.name(), "BLUE", unitId, type.name(), type.name(), Direction.DOWN
            ));

            assertTrue(result.accepted(), type.name());
            Unit unit = unit(result.state(), unitId);
            assertEquals(type.name(), unit.type());
            assertEquals(type == UnitType.SIEGE ? 2 : 1, unit.width());
            assertEquals(type == UnitType.SIEGE ? 2 : 1, unit.height());
        }
    }

    @Test
    void spawnsNamedUnitBesideHeadquartersAndLetsItAct() {
        GameSession session = new GameSession(DefaultMatchSetup.create());

        ApplyResult spawned = session.apply(new SpawnCommand(
                "spawn-1", "BLUE", "worker-blue-2", "Builder", Direction.DOWN
        ));
        ApplyResult moved = session.apply(new MoveCommand(
                "move-1", "BLUE", "worker-blue-2", Direction.RIGHT
        ));

        assertTrue(spawned.accepted());
        Unit unit = unit(moved.state(), "worker-blue-2");
        assertEquals("Builder", unit.displayName());
        assertEquals(new Position(1, 1), unit.position());
        assertEquals(3, unit.hp());
        assertEquals(2, moved.state().revision());
    }

    @Test
    void rejectsTakenIdOccupiedCellAndTeamLimit() {
        GameSession session = new GameSession(DefaultMatchSetup.create());

        assertEquals(ErrorCode.ENTITY_ID_TAKEN, session.apply(new SpawnCommand(
                "duplicate", "BLUE", "worker-blue", "Duplicate", Direction.DOWN
        )).error());
        assertEquals(ErrorCode.CELL_OCCUPIED, session.apply(new SpawnCommand(
                "occupied", "BLUE", "worker-blue-2", "Blocked", Direction.RIGHT
        )).error());
        assertEquals(ErrorCode.OUT_OF_BOUNDS, session.apply(new SpawnCommand(
                "outside", "BLUE", "worker-blue-2", "Outside", Direction.UP
        )).error());

        MatchSetup limitedSetup = DefaultMatchSetup.create();
        MatchSetup limitOne = new MatchSetup(
                limitedSetup.setupId(),
                limitedSetup.matchId(),
                limitedSetup.mapWidth(),
                limitedSetup.mapHeight(),
                new RuleSet("limit-one", 1, 1, 1, 3),
                limitedSetup.teams(),
                limitedSetup.units(),
                limitedSetup.headquarters()
        );
        assertEquals(ErrorCode.UNIT_LIMIT_REACHED, new GameSession(limitOne).apply(new SpawnCommand(
                "limit", "BLUE", "worker-blue-2", "Too Many", Direction.DOWN
        )).error());
    }

    @Test
    void removesDestroyedUnitFromSnapshot() {
        GameSession session = new GameSession(DefaultMatchSetup.create());
        MatchState initial = session.state();
        MatchSetup combat = new MatchSetup(
                initial.setupId(),
                initial.matchId(),
                initial.mapWidth(),
                initial.mapHeight(),
                new RuleSet("lethal", 3, 1, 5, 3),
                initial.teams(),
                java.util.List.of(
                        new Unit("blue", "BLUE", "WORKER", new Position(2, 2), 3, Direction.RIGHT),
                        new Unit("red", "RED", "WORKER", new Position(3, 2), 3, Direction.LEFT)
                ),
                initial.headquarters()
        );
        session = new GameSession(combat);

        ApplyResult attack = session.apply(new AttackCommand("lethal-hit", "BLUE", "blue", Direction.RIGHT));
        ApplyResult result = session.advanceTick();

        assertTrue(attack.accepted());
        assertTrue(result.accepted());
        assertFalse(result.state().units().stream().anyMatch(unit -> unit.id().equals("red")));
        assertTrue(result.state().retiredUnitIds().contains("red"));
        assertEquals(ErrorCode.ENTITY_ID_TAKEN, session.apply(new SpawnCommand(
                "reuse-retired", "BLUE", "red", "Reused", Direction.RIGHT
        )).error());
    }

    private Unit unit(MatchState state, String id) {
        return state.units().stream()
                .filter(unit -> unit.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
