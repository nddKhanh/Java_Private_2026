package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import java.util.List;

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
        assertEquals(3, team(harvested.state(), "BLUE").wood());
        assertEquals(7, resource(harvested.state(), "ore-center").amount());
        assertTrue(built.accepted());
        assertEquals(0, team(built.state(), "BLUE").wood());
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

    @Test
    void rejectsBuildingOnResourceNode() {
        GameSession session = new GameSession(DefaultMatchSetup.create());
        session.apply(new MoveCommand("ore-1", "BLUE", "worker-blue", Direction.RIGHT));
        session.apply(new MoveCommand("ore-2", "BLUE", "worker-blue", Direction.DOWN));
        session.apply(new MoveCommand("ore-3", "BLUE", "worker-blue", Direction.DOWN));
        session.apply(new HarvestCommand("harvest", "BLUE", "worker-blue"));
        session.apply(new MoveCommand("off-ore", "BLUE", "worker-blue", Direction.LEFT));

        assertEquals(ErrorCode.INVALID_BUILD_POSITION, session.apply(new BuildCommand(
                "wall-on-ore", "BLUE", "worker-blue", "wall-blue", BuildingType.WALL, Direction.RIGHT
        )).error());
    }

    @Test
    void spawnSpendsMeatAndHarvestFillsTheNodeType() {
        MatchSetup base = DefaultMatchSetup.create();
        GameSession session = new GameSession(new MatchSetup(
                base.setupId(), base.matchId(), base.mapWidth(), base.mapHeight(),
                new RuleSet("unit-cost", 1, 1, 5, 3, 0, 2, 3, 0, 3, 2, 1, 5, 4, 2, 2, 3, 5, 5),
                base.teams(), base.units(), base.headquarters(),
                List.of(new ResourceNode("meat-1", ResourceType.MEAT, new Position(2, 2), 10)), base.buildings()
        ));

        ApplyResult spawned = session.apply(new SpawnCommand(
                "spawn", "BLUE", "hq-blue", "worker-blue-2", "Worker", "WORKER", Direction.DOWN
        ));
        assertTrue(spawned.accepted());
        assertEquals(0, team(spawned.state(), "BLUE").meat());
        assertEquals(ErrorCode.INSUFFICIENT_RESOURCES, session.apply(new SpawnCommand(
                "spawn-broke", "BLUE", "hq-blue", "worker-blue-3", "Worker", "WORKER", Direction.UP
        )).error());

        session.apply(new MoveCommand("meat-1", "BLUE", "worker-blue", Direction.RIGHT));
        session.apply(new MoveCommand("meat-2", "BLUE", "worker-blue", Direction.DOWN));
        session.apply(new MoveCommand("meat-3", "BLUE", "worker-blue", Direction.DOWN));
        ApplyResult harvested = session.apply(new HarvestCommand("harvest", "BLUE", "worker-blue"));
        assertEquals(3, team(harvested.state(), "BLUE").meat());
        assertEquals(0, team(harvested.state(), "BLUE").wood());
    }

    @Test
    void siegeCostsWoodAndMeat() {
        RuleSet rules = new RuleSet("siege", 1, 1, 5, 3, 0, 0, 3, 0, 3, 2, 1, 5, 4, 1, 2, 3, 5, 4);

        assertEquals(new Cost(4, 5), rules.unitCost(UnitType.SIEGE));
        assertEquals(new Cost(0, 2), rules.unitCost(UnitType.MELEE));
        assertEquals(new Cost(3, 0), rules.buildingCost(BuildingType.BARRACKS));
    }

    @Test
    void starterPackPlacesEntitiesAroundHeadquartersAndMirrorsInward() {
        StarterPack pack = new StarterPack(
                List.of(new StarterPack.PackResource(ResourceType.MEAT, 0, 2, 50)),
                List.of(new StarterPack.PackBuilding(BuildingType.TOWER, 2, 0)),
                List.of(new StarterPack.PackUnit(UnitType.WORKER, 1, 0))
        );
        MatchSetup setup = pack.applyTo(new MatchSetup(
                "pack", "pack", 10, 10, RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue"), new Team("RED", "Red", "red")),
                List.of(),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(9, 9), 3)
                )
        ));
        MatchState state = MatchFactory.create(setup);

        assertEquals(new Position(1, 0), unitAt(state, "worker-blue"));
        assertEquals(new Position(8, 9), unitAt(state, "worker-red"));
        assertEquals(new Position(9, 7), resource(state, "meat-red").position());
        assertEquals(new Position(7, 9), building(state, "home-tower-red").position());
    }

    private Position unitAt(MatchState state, String id) {
        return state.units().stream().filter(unit -> unit.id().equals(id)).findFirst().orElseThrow().position();
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
