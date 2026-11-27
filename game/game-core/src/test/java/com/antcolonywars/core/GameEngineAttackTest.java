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
        assertEquals(ErrorCode.TARGET_NOT_FOUND, friendly.error());

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
    void attackSkipsAlliedUnitsIncludingAnotherTeamInTheSameFaction() {
        MatchSetup setup = new MatchSetup(
                "allied-targeting", "allied-targeting", 7, 4, new RuleSet("allied-targeting", 1, 6),
                List.of(
                        new Team("BLUE", "Blue", "blue", "ALLIANCE"),
                        new Team("CYAN", "Cyan", "cyan", "ALLIANCE"),
                        new Team("RED", "Red", "red", "SWARM")
                ),
                List.of(
                        new Unit("ranged-blue", "BLUE", "RANGED", new Position(0, 1), 3, Direction.RIGHT),
                        new Unit("ally-blue", "BLUE", "WORKER", new Position(1, 1), 3, Direction.RIGHT),
                        new Unit("ally-cyan", "CYAN", "WORKER", new Position(2, 1), 3, Direction.RIGHT),
                        new Unit("enemy-red", "RED", "WORKER", new Position(4, 1), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-cyan", "CYAN", new Position(3, 1), 3),
                        new Headquarters("hq-red", "RED", new Position(6, 3), 3)
                )
        );
        GameSession session = new GameSession(setup);

        ApplyResult attack = session.apply(new AttackCommand(
                "skip-allies", "BLUE", "ranged-blue", Direction.RIGHT
        ));
        for (int tick = 0; tick < 4; tick++) {
            session.advanceTick();
        }

        assertTrue(attack.accepted());
        assertEquals("enemy-red", attack.events().getFirst().targetId());
        assertEquals(3, unit(session.state(), "ally-blue").hp());
        assertEquals(3, unit(session.state(), "ally-cyan").hp());
        assertEquals(2, unit(session.state(), "enemy-red").hp());
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
    void towerAutomaticallyFiresWithinFiveCellsAlongAFourConnectedPath() {
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

        for (int index = 0; index < 4; index++) {
            session.advanceTick();
        }
        MatchState tick = session.advanceTick().state();

        assertEquals(5, tick.buildings().stream()
                .filter(building -> building.id().equals("wall-blue"))
                .findFirst().orElseThrow().hp());
        assertEquals(2, unit(tick, "worker-red").hp());
    }

    @Test
    void towerFiresAtMostOnceEveryFourDefaultTicks() {
        MatchSetup setup = new MatchSetup(
                "tower-cooldown", "tower-cooldown", 6, 6, RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue"), new Team("RED", "Red", "red")),
                List.of(new Unit("worker-red", "RED", "WORKER", new Position(3, 1), 5, Direction.LEFT)),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(5, 5), 3)
                ),
                List.of(),
                List.of(new Building("tower-blue", "BLUE", BuildingType.TOWER, new Position(0, 1), 5))
        );
        GameSession session = new GameSession(setup);

        for (int tick = 0; tick < 4; tick++) {
            session.advanceTick();
        }

        assertEquals(4, unit(session.state(), "worker-red").hp());
        assertTrue(session.state().projectiles().isEmpty());

        session.advanceTick();
        assertEquals(1, session.state().projectiles().size());
        assertEquals(4, unit(session.state(), "worker-red").hp());
    }

    @Test
    void enemyUnitsAndHeadquartersBlockProjectilesOnTheirFixedPath() {
        MatchSetup unitBlock = new MatchSetup(
                "unit-block", "unit-block", 6, 6, RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue"), new Team("RED", "Red", "red")),
                List.of(
                        new Unit("blocker-red", "RED", "WORKER", new Position(1, 1), 3, Direction.RIGHT),
                        new Unit("target-red", "RED", "WORKER", new Position(3, 1), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(5, 5), 3)
                ),
                List.of(),
                List.of(new Building("tower-blue", "BLUE", BuildingType.TOWER, new Position(0, 1), 5))
        );
        GameSession unitSession = new GameSession(unitBlock);
        unitSession.advanceTick();
        assertEquals(2, unit(unitSession.state(), "blocker-red").hp());
        assertEquals(3, unit(unitSession.state(), "target-red").hp());

        MatchSetup headquartersBlock = new MatchSetup(
                "hq-block", "hq-block", 6, 6, RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue"), new Team("RED", "Red", "red")),
                List.of(new Unit("target-red", "RED", "WORKER", new Position(3, 1), 3, Direction.LEFT)),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(1, 1), 3)
                ),
                List.of(),
                List.of(new Building("tower-blue", "BLUE", BuildingType.TOWER, new Position(0, 1), 5))
        );
        GameSession headquartersSession = new GameSession(headquartersBlock);
        headquartersSession.advanceTick();
        assertEquals(2, headquarters(headquartersSession.state(), "hq-red").hp());
        assertEquals(3, unit(headquartersSession.state(), "target-red").hp());
    }

    @Test
    void projectilesPassThroughEveryAlliedEntityAndHitTheEnemyBehindThem() {
        MatchSetup setup = new MatchSetup(
                "allied-pass", "allied-pass", 8, 4, new RuleSet("allied-pass", 1, 7),
                List.of(
                        new Team("BLUE", "Blue", "blue", "ALLIANCE"),
                        new Team("CYAN", "Cyan", "cyan", "ALLIANCE"),
                        new Team("RED", "Red", "red", "SWARM")
                ),
                List.of(
                        new Unit("ranged-blue", "BLUE", "RANGED", new Position(0, 1), 3, Direction.RIGHT),
                        new Unit("ally-blue", "BLUE", "WORKER", new Position(2, 1), 3, Direction.RIGHT),
                        new Unit("ally-cyan", "CYAN", "WORKER", new Position(5, 1), 3, Direction.RIGHT),
                        new Unit("enemy-red", "RED", "WORKER", new Position(7, 1), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-cyan", "CYAN", new Position(4, 1), 3),
                        new Headquarters("hq-red", "RED", new Position(7, 3), 3)
                ),
                List.of(),
                List.of(
                        new Building("wall-blue", "BLUE", BuildingType.WALL, new Position(1, 1), 5),
                        new Building("tower-cyan", "CYAN", BuildingType.TOWER, new Position(3, 1), 5),
                        new Building("wall-cyan", "CYAN", BuildingType.WALL, new Position(6, 1), 5)
                )
        );
        MatchState base = MatchFactory.create(setup);
        MatchState current = new MatchState(
                base.matchId(), base.setupId(), base.rulesVersion(), base.status(), base.winnerTeamId(),
                base.revision(), base.eventSequence(), base.simulationTick(), base.mapWidth(), base.mapHeight(),
                base.teams(), base.units(), base.retiredUnitIds(), base.headquarters(), base.resourceNodes(),
                base.buildings(), List.of(new Projectile(
                        "allied-pass-shot", "BLUE", "ranged-blue", "enemy-red",
                        new Position(0, 1), new Position(7, 1), 1
                )), base.processedCommandIds()
        );
        GameEngine engine = new GameEngine();

        for (int tick = 0; tick < 7; tick++) {
            current = engine.apply(current, new TickCommand("allied-pass-tick-" + tick), setup.rules()).state();
        }

        assertEquals(3, unit(current, "ally-blue").hp());
        assertEquals(3, unit(current, "ally-cyan").hp());
        assertEquals(3, headquarters(current, "hq-cyan").hp());
        assertTrue(current.buildings().stream()
                .filter(building -> building.teamId().equals("BLUE") || building.teamId().equals("CYAN"))
                .allMatch(building -> building.hp() == 5));
        assertTrue(unit(current, "enemy-red").hp() < 3);
    }

    @Test
    void projectilePathNeverStepsDiagonally() {
        Projectile projectile = new Projectile(
                "diagonal", "BLUE", "worker-blue", "worker-red", new Position(0, 0), new Position(2, 3), 1
        );

        for (int index = 1; index < projectile.path().size(); index++) {
            assertEquals(1, projectile.path().get(index - 1).manhattanDistance(projectile.path().get(index)));
        }
        assertEquals(new Position(2, 3), projectile.path().getLast());
    }

    @Test
    void unitMovingIntoEnemyProjectileIsHit() {
        GameSession session = new GameSession(combatSetup(
                new Position(1, 1), new Position(4, 1), new RuleSet("range", 1, 3), new Position(4, 4)
        ));
        session.apply(new AttackCommand("shot", "BLUE", "worker-blue", Direction.RIGHT));
        session.advanceTick();

        session.apply(new MoveCommand("dodge-1", "RED", "worker-red", Direction.LEFT));
        ApplyResult swapped = session.apply(new MoveCommand("dodge-2", "RED", "worker-red", Direction.LEFT));

        assertEquals(2, unit(swapped.state(), "worker-red").hp());
        assertTrue(swapped.state().projectiles().isEmpty());
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
