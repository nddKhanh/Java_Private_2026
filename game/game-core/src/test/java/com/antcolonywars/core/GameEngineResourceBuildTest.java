package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineResourceBuildTest {
    @Test
    void harvestsResourceAndBuildsWithTheTeamStockpile() {
        GameSession session = new GameSession(DefaultMatchSetup.create());
        session.apply(new MoveCommand("to-ore-1", "BLUE", "worker-blue", Direction.RIGHT));
        session.apply(new MoveCommand("to-ore-2", "BLUE", "worker-blue", Direction.DOWN));
        session.apply(new MoveCommand("to-ore-3", "BLUE", "worker-blue", Direction.DOWN));

        ApplyResult harvested = session.apply(new HarvestCommand("harvest-1", "BLUE", "worker-blue"));
        ApplyResult built = session.apply(new BuildCommand(
                "build-1", "BLUE", "worker-blue", "barracks-blue", BuildingType.BARRACKS, Direction.RIGHT
        ));

        assertTrue(harvested.accepted());
        assertEquals(3, team(harvested.state(), "BLUE").resources());
        assertEquals(7, resource(harvested.state(), "ore-center").amount());
        assertTrue(built.accepted());
        assertEquals(0, team(built.state(), "BLUE").resources());
        assertEquals(new Position(3, 2), building(built.state(), "barracks-blue").position());
        assertEquals(BuildingType.BARRACKS, building(built.state(), "barracks-blue").type());
    }

    @Test
    void rejectsBuildWithoutResourcesAndRejectsDepletedResource() {
        GameSession session = new GameSession(DefaultMatchSetup.create());

        assertEquals(ErrorCode.INSUFFICIENT_RESOURCES, session.apply(new BuildCommand(
                "build-too-early", "BLUE", "worker-blue", "wall-blue", BuildingType.WALL, Direction.RIGHT
        )).error());

        session.apply(new MoveCommand("ore-1", "BLUE", "worker-blue", Direction.RIGHT));
        session.apply(new MoveCommand("ore-2", "BLUE", "worker-blue", Direction.DOWN));
        session.apply(new MoveCommand("ore-3", "BLUE", "worker-blue", Direction.DOWN));
        for (int index = 0; index < 4; index++) {
            session.apply(new HarvestCommand("harvest-" + index, "BLUE", "worker-blue"));
        }
        assertEquals(ErrorCode.RESOURCE_DEPLETED,
                session.apply(new HarvestCommand("harvest-empty", "BLUE", "worker-blue")).error());
    }

    private Team team(MatchState state, String id) {
        return state.teams().stream().filter(team -> team.id().equals(id)).findFirst().orElseThrow();
    }

    private ResourceNode resource(MatchState state, String id) {
        return state.resourceNodes().stream().filter(node -> node.id().equals(id)).findFirst().orElseThrow();
    }

    private Building building(MatchState state, String id) {
        return state.buildings().stream().filter(item -> item.id().equals(id)).findFirst().orElseThrow();
    }
}
