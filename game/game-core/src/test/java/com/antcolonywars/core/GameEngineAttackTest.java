package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineAttackTest {
    @Test
    void rejectsWhenNoTargetIsFoundInTheAttackDirectionAndRange() {
        GameSession session = new GameSession(combatSetup(new Position(1, 1), new Position(3, 1)));
        MatchState before = session.state();

        ApplyResult result = session.apply(
                new AttackCommand("attack-far", "BLUE", "worker-blue", Direction.RIGHT)
        );

        assertEquals(ErrorCode.TARGET_NOT_FOUND, result.error());
        assertSame(before, result.state());
    }

    @Test
    void damagesAdjacentEnemyByConfiguredAmount() {
        RuleSet rules = new RuleSet("damage-2", 2, 1);
        GameSession session = new GameSession(combatSetup(
                new Position(1, 1),
                new Position(2, 1),
                rules,
                new Position(4, 4)
        ));

        ApplyResult result = session.apply(
                new AttackCommand("attack-unit", "BLUE", "worker-blue", Direction.RIGHT)
        );
        ApplyResult tick = session.advanceTick();

        assertTrue(result.accepted());
        assertEquals(3, unit(result.state(), "worker-red").hp());
        assertEquals(1, result.state().projectiles().size());
        assertEquals(1, unit(tick.state(), "worker-red").hp());
        assertEquals(0, tick.state().projectiles().size());
        assertEquals(1, result.state().revision());
    }

    @Test
    void destroyingHeadquartersFinishesMatchAndOnlyGetStateStillSucceeds() {
        GameSession session = new GameSession(combatSetup(
                new Position(1, 1),
                new Position(3, 3),
                RuleSet.mvp(),
                new Position(2, 1)
        ));

        session.apply(new AttackCommand("hq-1", "BLUE", "worker-blue", Direction.RIGHT));
        session.apply(new AttackCommand("hq-2", "BLUE", "worker-blue", Direction.RIGHT));
        session.apply(new AttackCommand("hq-3", "BLUE", "worker-blue", Direction.RIGHT));
        ApplyResult finalHit = session.advanceTick();

        assertTrue(finalHit.accepted());
        assertEquals(MatchStatus.FINISHED, finalHit.state().status());
        assertEquals("BLUE", finalHit.state().winnerTeamId());
        assertEquals(0, headquarters(finalHit.state(), "hq-red").hp());

        ApplyResult actionAfterFinish = session.apply(new WaitCommand("too-late", "BLUE", "worker-blue"));
        ApplyResult readAfterFinish = session.apply(new GetStateCommand("read-finished"));

        assertEquals(ErrorCode.MATCH_FINISHED, actionAfterFinish.error());
        assertTrue(readAfterFinish.accepted());
        assertSame(finalHit.state(), readAfterFinish.state());
    }

    @Test
    void appliesRangedRangeAndSiegeDamageProfiles() {
        MatchSetup setup = new MatchSetup(
                "type-setup", "type-match", 6, 6, RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue"), new Team("RED", "Red", "red")),
                List.of(
                        new Unit("ranged-blue", "BLUE", "RANGED", new Position(1, 1), 3, Direction.RIGHT),
                        new Unit("siege-blue", "BLUE", "SIEGE", new Position(1, 3), 3, Direction.RIGHT),
                        new Unit("worker-red", "RED", "WORKER", new Position(3, 1), 3, Direction.LEFT),
                        new Unit("worker-red-2", "RED", "WORKER", new Position(3, 3), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(5, 5), 3)
                )
        );
        GameSession session = new GameSession(setup);

        ApplyResult ranged = session.apply(new AttackCommand(
                "ranged-attack", "BLUE", "ranged-blue", Direction.RIGHT));
        assertTrue(ranged.accepted());
        assertEquals(3, unit(ranged.state(), "worker-red").hp());

        ApplyResult siege = session.apply(new AttackCommand(
                "siege-attack", "BLUE", "siege-blue", Direction.RIGHT));
        assertTrue(siege.accepted());
        ApplyResult firstTick = session.advanceTick();
        assertEquals(1, unit(firstTick.state(), "worker-red-2").hp());
        ApplyResult secondTick = session.advanceTick();
        assertEquals(2, unit(secondTick.state(), "worker-red").hp());
    }

    @Test
    void treatsFactionMembersAsAlliesAndFinishesAfterTheEnemyFactionLosesEveryHeadquarters() {
        MatchSetup setup = new MatchSetup(
                "faction-setup", "faction-match", 5, 5, RuleSet.mvp(),
                List.of(
                        new Team("BLUE", "Blue", "blue", "ALLIANCE"),
                        new Team("CYAN", "Cyan", "cyan", "ALLIANCE"),
                        new Team("RED", "Red", "red", "SWARM"),
                        new Team("ORANGE", "Orange", "orange", "SWARM")
                ),
                List.of(
                        new Unit("worker-blue", "BLUE", "WORKER", new Position(1, 1), 3, Direction.RIGHT),
                        new Unit("worker-cyan", "CYAN", "WORKER", new Position(2, 1), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-cyan", "CYAN", new Position(0, 4), 3),
                        new Headquarters("hq-red", "RED", new Position(1, 2), 3),
                        new Headquarters("hq-orange", "ORANGE", new Position(0, 1), 3)
                )
        );
        GameSession session = new GameSession(setup);

        ApplyResult friendly = session.apply(new AttackCommand(
                "friendly", "BLUE", "worker-blue", Direction.RIGHT));
        assertEquals(ErrorCode.FRIENDLY_TARGET, friendly.error());

        for (int hit = 0; hit < 3; hit++) {
            assertTrue(session.apply(new AttackCommand(
                    "red-" + hit, "BLUE", "worker-blue", Direction.DOWN)).accepted());
        }
        MatchState afterRed = session.advanceTick().state();
        assertEquals(MatchStatus.RUNNING, afterRed.status());

        for (int hit = 0; hit < 3; hit++) {
            assertTrue(session.apply(new AttackCommand(
                    "orange-" + hit, "BLUE", "worker-blue", Direction.LEFT)).accepted());
        }
        MatchState finished = session.advanceTick().state();
        assertEquals(MatchStatus.FINISHED, finished.status());
        assertEquals("BLUE", finished.winnerTeamId());
    }

    @Test
    void projectileKeepsOriginalTargetPositionAndCanMissMovingUnit() {
        GameSession session = new GameSession(combatSetup(
                new Position(1, 1), new Position(3, 1), new RuleSet("range", 1, 3), new Position(4, 4)
        ));

        assertTrue(session.apply(new AttackCommand(
                "fixed-shot", "BLUE", "worker-blue", Direction.RIGHT
        )).accepted());
        assertTrue(session.apply(new MoveCommand(
                "dodge", "RED", "worker-red", Direction.DOWN
        )).accepted());
        session.advanceTick();
        MatchState afterMiss = session.advanceTick().state();

        assertEquals(3, unit(afterMiss, "worker-red").hp());
        assertTrue(afterMiss.projectiles().isEmpty());
    }

    @Test
    void buildingBlocksProjectileTakesDamageAndIsRemovedAtZeroHp() {
        MatchSetup base = combatSetup(
                new Position(1, 1), new Position(4, 1), new RuleSet("building-hit", 2, 4), new Position(4, 4)
        );
        MatchSetup setup = new MatchSetup(
                base.setupId(), base.matchId(), base.mapWidth(), base.mapHeight(), base.rules(),
                base.teams(), base.units(), base.headquarters(), base.resourceNodes(),
                List.of(new Building("wall-red", "RED", BuildingType.WALL, new Position(2, 1), 2))
        );
        GameSession session = new GameSession(setup);

        assertTrue(session.apply(new AttackCommand(
                "wall-shot", "BLUE", "worker-blue", Direction.RIGHT
        )).accepted());
        MatchState hit = session.advanceTick().state();

        assertTrue(hit.buildings().isEmpty());
        assertEquals(3, unit(hit, "worker-red").hp());
        assertTrue(hit.projectiles().isEmpty());
    }

    @Test
    void towerAutomaticallyFiresWithinFiveCellsAndUsesDiagonalBresenhamCell() {
        MatchSetup base = combatSetup(
                new Position(4, 0), new Position(3, 3), RuleSet.mvp(), new Position(5, 5)
        );
        MatchSetup setup = new MatchSetup(
                base.setupId(), base.matchId(), 6, 6, base.rules(),
                base.teams(), base.units(), base.headquarters(), base.resourceNodes(),
                List.of(
                        new Building("tower-blue", "BLUE", BuildingType.TOWER, new Position(0, 1), 5),
                        new Building("wall-blue", "BLUE", BuildingType.WALL, new Position(1, 2), 5)
                )
        );
        GameSession session = new GameSession(setup);

        MatchState tick = session.advanceTick().state();

        assertEquals(4, tick.buildings().stream()
                .filter(building -> building.id().equals("wall-blue"))
                .findFirst().orElseThrow().hp());
        assertEquals(3, unit(tick, "worker-red").hp());
    }

    private MatchSetup combatSetup(Position blue, Position red) {
        return combatSetup(blue, red, RuleSet.mvp(), new Position(4, 4));
    }

    private MatchSetup combatSetup(Position blue, Position red, RuleSet rules, Position redHeadquarters) {
        return new MatchSetup(
                "combat-setup",
                "combat-match",
                5,
                5,
                rules,
                List.of(
                        new Team("BLUE", "Blue", "blue"),
                        new Team("RED", "Red", "red")
                ),
                List.of(
                        new Unit("worker-blue", "BLUE", "WORKER", blue, 3, Direction.RIGHT),
                        new Unit("worker-red", "RED", "WORKER", red, 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", redHeadquarters, 3)
                )
        );
    }

    private Unit unit(MatchState state, String id) {
        return state.units().stream().filter(unit -> unit.id().equals(id)).findFirst().orElseThrow();
    }

    private Headquarters headquarters(MatchState state, String id) {
        return state.headquarters().stream()
                .filter(headquarters -> headquarters.id().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
