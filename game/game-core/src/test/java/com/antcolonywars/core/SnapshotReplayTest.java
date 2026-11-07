package com.antcolonywars.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class SnapshotReplayTest {
    @Test
    void snapshotRoundTripPreservesGameplayAndIdempotencyData() {
        GameSession session = new GameSession(DefaultMatchSetup.create());
        session.apply(new MoveCommand("move-1", "BLUE", "worker-blue", Direction.RIGHT));
        session.apply(new WaitCommand("wait-1", "BLUE", "worker-blue"));
        SnapshotCodec codec = new SnapshotCodec();

        MatchState restored = codec.deserialize(codec.serialize(session.state()));

        assertEquals(session.state(), restored);
        assertEquals(codec.stateHash(session.state()), codec.stateHash(restored));
    }

    @Test
    void replayProducesSameStateHashAsOriginalCommandLog() {
        MatchSetup setup = DefaultMatchSetup.create();
        GameSession original = new GameSession(setup);
        original.apply(new MoveCommand("move-1", "BLUE", "worker-blue", Direction.RIGHT));
        original.apply(new MoveCommand("move-2", "BLUE", "worker-blue", Direction.DOWN));
        original.apply(new WaitCommand("wait-1", "RED", "worker-red"));

        MatchState replayed = new ReplayService().replay(setup, original.commandLog());
        SnapshotCodec codec = new SnapshotCodec();

        assertEquals(original.state(), replayed);
        assertEquals(codec.stateHash(original.state()), codec.stateHash(replayed));
    }

    @Test
    void sameCommandsAreDeterministicAndDifferentSetupsRemainConfigurable() {
        List<ActionCommand> commands = List.of(
                new MoveCommand("move-1", "BLUE", "worker-blue", Direction.RIGHT),
                new WaitCommand("wait-1", "RED", "worker-red")
        );
        MatchSetup defaultSetup = DefaultMatchSetup.create();
        MatchSetup largerSetup = new MatchSetup(
                "large-profile",
                "large-match",
                12,
                10,
                new RuleSet("large-rules", 2, 2),
                defaultSetup.teams(),
                defaultSetup.units(),
                List.of(
                        new Headquarters("hq-blue", "BLUE", new Position(0, 0), 5),
                        new Headquarters("hq-red", "RED", new Position(11, 9), 5)
                )
        );
        ReplayService replayService = new ReplayService();
        SnapshotCodec codec = new SnapshotCodec();

        MatchState first = replayService.replay(defaultSetup, commands);
        MatchState second = replayService.replay(defaultSetup, commands);
        MatchState configured = MatchFactory.create(largerSetup);

        assertEquals(codec.stateHash(first), codec.stateHash(second));
        assertEquals(12, configured.mapWidth());
        assertEquals(10, configured.mapHeight());
        assertEquals("large-rules", configured.rulesVersion());
        assertNotEquals(codec.stateHash(first), codec.stateHash(configured));
    }
}
