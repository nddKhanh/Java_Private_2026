package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GameEngineMovementTest {
    @Test
    void movesExactlyOneCellInEveryDirection() {
        for (Direction direction : Direction.values()) {
            GameSession session = new GameSession(setupWithBlueAt(new Position(2, 2)));

            ApplyResult result = session.apply(new MoveCommand("move-" + direction, "BLUE", "worker-blue", direction));

            assertTrue(result.accepted());
            assertEquals(new Position(2, 2).move(direction), unit(result.state(), "worker-blue").position());
            assertEquals(1, result.state().revision());
            assertEquals(1, result.state().eventSequence());
        }
    }

    @Test
    void rejectsOutOfBoundsAndOccupiedMovesWithoutChangingState() {
        GameSession outOfBoundsSession = new GameSession(setupWithBlueAt(new Position(0, 1)));
        MatchState outOfBoundsState = outOfBoundsSession.state();

        ApplyResult outOfBounds = outOfBoundsSession.apply(
                new MoveCommand("outside", "BLUE", "worker-blue", Direction.LEFT)
        );

        assertEquals(ErrorCode.OUT_OF_BOUNDS, outOfBounds.error());
        assertSame(outOfBoundsState, outOfBounds.state());

        GameSession occupiedSession = new GameSession(setupWithBlueAt(new Position(1, 0)));
        MatchState occupiedState = occupiedSession.state();
        ApplyResult occupied = occupiedSession.apply(
                new MoveCommand("occupied", "BLUE", "worker-blue", Direction.LEFT)
        );

        assertEquals(ErrorCode.CELL_OCCUPIED, occupied.error());
        assertSame(occupiedState, occupied.state());
    }

    @Test
    void waitAdvancesRevisionWithoutMovingAndDuplicateIsNotAppliedTwice() {
        GameSession session = new GameSession(setupWithBlueAt(new Position(2, 2)));
        Position initialPosition = unit(session.state(), "worker-blue").position();
        WaitCommand command = new WaitCommand("wait-1", "BLUE", "worker-blue");

        ApplyResult first = session.apply(command);
        ApplyResult duplicate = session.apply(command);

        assertTrue(first.accepted());
        assertEquals(initialPosition, unit(first.state(), "worker-blue").position());
        assertEquals(1, first.state().revision());
        assertEquals(ErrorCode.DUPLICATE_COMMAND, duplicate.error());
        assertSame(first.state(), duplicate.state());
    }

    @Test
    void rejectsCommandForUnitOwnedByAnotherTeam() {
        GameSession session = new GameSession(setupWithBlueAt(new Position(2, 2)));
        MatchState before = session.state();

        ApplyResult result = session.apply(new WaitCommand("steal", "RED", "worker-blue"));

        assertEquals(ErrorCode.NOT_OWNER, result.error());
        assertSame(before, result.state());
    }

    private MatchSetup setupWithBlueAt(Position bluePosition) {
        return new MatchSetup(
                "movement-setup",
                "movement-match",
                5,
                5,
                RuleSet.mvp(),
                List.of(
                        new Team("BLUE", "Blue", "blue"),
                        new Team("RED", "Red", "red")
                ),
                List.of(
                        new Unit("worker-blue", "BLUE", "WORKER", bluePosition, 3, Direction.RIGHT),
                        new Unit("worker-red", "RED", "WORKER", new Position(3, 3), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(4, 4), 3)
                )
        );
    }

    private Unit unit(MatchState state, String id) {
        return state.units().stream().filter(unit -> unit.id().equals(id)).findFirst().orElseThrow();
    }
}
