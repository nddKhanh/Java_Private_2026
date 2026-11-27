package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandQueueTest {
    @Test
    void runsOneCommandPerBotPerTick() {
        GameSession session = new GameSession(setup());
        CommandQueue queue = new CommandQueue(40);
        queue.enqueue("batch-1", "BLUE", List.of(
                move("a-1", "worker-a", Direction.RIGHT),
                move("a-2", "worker-a", Direction.RIGHT),
                move("b-1", "worker-b", Direction.LEFT)
        ));

        session.advanceTick(queue);

        assertEquals(new Position(3, 2), position(session.state(), "worker-a"));
        assertEquals(new Position(4, 5), position(session.state(), "worker-b"));
        assertEquals(1, queue.size("BLUE", "worker-a"));
        assertEquals(0, queue.size("BLUE", "worker-b"));

        session.advanceTick(queue);

        assertEquals(new Position(4, 2), position(session.state(), "worker-a"));
        assertTrue(queue.isEmpty());
    }

    @Test
    void failedCommandDropsOnlyTheRestOfItsBatchForThatBot() {
        GameSession session = new GameSession(setup());
        CommandQueue queue = new CommandQueue(40);
        queue.enqueue("first", "BLUE", List.of(
                move("up-1", "worker-a", Direction.UP),
                move("up-2", "worker-a", Direction.UP),
                move("up-3", "worker-a", Direction.UP),
                move("after-wall", "worker-a", Direction.RIGHT),
                move("other-bot", "worker-b", Direction.LEFT),
                move("other-bot-2", "worker-b", Direction.LEFT)
        ));
        queue.enqueue("second", "BLUE", List.of(move("teammate", "worker-a", Direction.DOWN)));

        session.advanceTick(queue);
        session.advanceTick(queue);
        QueueTickResult failed = session.advanceTick(queue);

        assertEquals(1, failed.rejections().size());
        CommandRejection rejection = failed.rejections().getFirst();
        assertEquals("up-3", rejection.commandId());
        assertEquals(ErrorCode.OUT_OF_BOUNDS, rejection.error());
        assertEquals(1, rejection.droppedCount());
        assertEquals(new Position(2, 0), position(session.state(), "worker-a"));
        assertEquals(new Position(3, 5), position(session.state(), "worker-b"));

        session.advanceTick(queue);

        assertEquals(new Position(2, 1), position(session.state(), "worker-a"));
        assertTrue(queue.isEmpty());
    }

    @Test
    void refusesTheWholeShareOfABotWhoseQueueWouldOverflow() {
        CommandQueue queue = new CommandQueue(2);
        queue.enqueue("first", "BLUE", List.of(move("a-1", "worker-a", Direction.RIGHT)));

        List<String> refused = queue.enqueue("second", "BLUE", List.of(
                move("a-2", "worker-a", Direction.RIGHT),
                move("a-3", "worker-a", Direction.RIGHT),
                move("b-1", "worker-b", Direction.LEFT)
        ));

        assertEquals(List.of("worker-a"), refused);
        assertEquals(1, queue.size("BLUE", "worker-a"));
        assertEquals(1, queue.size("BLUE", "worker-b"));
    }

    @Test
    void spawnWaitsForTheBarracksBuiltEarlierInTheSameBatch() {
        GameSession session = new GameSession(setup());
        CommandQueue queue = new CommandQueue(40);
        queue.enqueue("build-then-spawn", "BLUE", List.of(
                move("step", "worker-a", Direction.DOWN),
                new BuildCommand("build", "BLUE", "worker-a", "barracks-blue", BuildingType.BARRACKS, Direction.RIGHT),
                new SpawnCommand("spawn", "BLUE", "barracks-blue", "melee-blue", "Melee", "MELEE", Direction.DOWN)
        ));

        QueueTickResult first = session.advanceTick(queue);
        session.advanceTick(queue);
        session.advanceTick(queue);

        assertTrue(first.rejections().isEmpty());
        assertEquals(new Position(3, 4), position(session.state(), "melee-blue"));
        assertTrue(queue.isEmpty());
    }

    @Test
    void clearRemovesOnlyTheTeamsOwnQueueForThatBot() {
        CommandQueue queue = new CommandQueue(40);
        queue.enqueue("blue", "BLUE", List.of(move("a-1", "worker-a", Direction.RIGHT)));
        queue.enqueue("red", "RED", List.of(move("steal", "worker-a", Direction.LEFT)));

        assertEquals(1, queue.clear("BLUE", "worker-a"));
        assertEquals(0, queue.size("BLUE", "worker-a"));
        assertEquals(1, queue.size("RED", "worker-a"));
    }

    @Test
    void finishedMatchEmptiesEveryQueue() {
        GameSession session = new GameSession(new MatchSetup(
                "finish", "finish", 6, 6, RuleSet.mvp(),
                List.of(new Team("BLUE", "Blue", "blue", "A"), new Team("RED", "Red", "red", "B")),
                List.of(new Unit("worker-a", "BLUE", "WORKER", new Position(3, 4), 3, Direction.RIGHT)),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(4, 4), 1)
                )
        ));
        CommandQueue queue = new CommandQueue(40);
        queue.enqueue("finish", "BLUE", List.of(
                new AttackCommand("hit", "BLUE", "worker-a", Direction.RIGHT),
                new AttackCommand("hit-again", "BLUE", "worker-a", Direction.RIGHT),
                new WaitCommand("wait", "BLUE", "worker-a")
        ));

        session.advanceTick(queue);

        assertEquals(MatchStatus.FINISHED, session.state().status());
        assertTrue(queue.isEmpty());
    }

    private static MoveCommand move(String id, String unitId, Direction direction) {
        return new MoveCommand(id, "BLUE", unitId, direction);
    }

    private static MatchSetup setup() {
        return new MatchSetup(
                "queue", "queue", 8, 8,
                new RuleSet("queue", 1, 1, 5, 3, 20, 3, 0, 3, 2, 1, 5, 4),
                List.of(new Team("BLUE", "Blue", "blue", "A"), new Team("RED", "Red", "red", "B")),
                List.of(
                        new Unit("worker-a", "BLUE", "WORKER", new Position(2, 2), 3, Direction.RIGHT),
                        new Unit("worker-b", "BLUE", "WORKER", new Position(5, 5), 3, Direction.LEFT)
                ),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 3),
                        new Headquarters("hq-red", "RED", new Position(7, 7), 3)
                )
        );
    }

    private static Position position(MatchState state, String unitId) {
        return state.units().stream().filter(unit -> unit.id().equals(unitId)).findFirst().orElseThrow().position();
    }
}
