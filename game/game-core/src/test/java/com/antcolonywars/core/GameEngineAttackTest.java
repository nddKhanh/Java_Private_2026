package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineAttackTest {
    @Test
    void rejectsTargetOutsideAttackRange() {
        GameSession session = new GameSession(combatSetup(new Position(1, 1), new Position(3, 3)));
        MatchState before = session.state();

        ApplyResult result = session.apply(
                new AttackCommand("attack-far", "BLUE", "worker-blue", "worker-red")
        );

        assertEquals(ErrorCode.OUT_OF_RANGE, result.error());
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
                new AttackCommand("attack-unit", "BLUE", "worker-blue", "worker-red")
        );

        assertTrue(result.accepted());
        assertEquals(1, unit(result.state(), "worker-red").hp());
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

        session.apply(new AttackCommand("hq-1", "BLUE", "worker-blue", "hq-red"));
        session.apply(new AttackCommand("hq-2", "BLUE", "worker-blue", "hq-red"));
        ApplyResult finalHit = session.apply(new AttackCommand("hq-3", "BLUE", "worker-blue", "hq-red"));

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
                        new Unit("siege-blue", "BLUE", "SIEGE", new Position(2, 1), 3, Direction.RIGHT),
                        new Unit("worker-red", "RED", "WORKER", new Position(3, 1), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(5, 5), 3)
                )
        );
        GameSession session = new GameSession(setup);

        ApplyResult ranged = session.apply(new AttackCommand(
                "ranged-attack", "BLUE", "ranged-blue", "worker-red"));
        assertTrue(ranged.accepted());
        assertEquals(2, unit(ranged.state(), "worker-red").hp());

        ApplyResult siege = session.apply(new AttackCommand(
                "siege-attack", "BLUE", "siege-blue", "worker-red"));
        assertTrue(siege.accepted());
        assertTrue(siege.state().retiredUnitIds().contains("worker-red"));
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
