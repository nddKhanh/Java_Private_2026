package com.antcolonywars.client;

import com.antcolonywars.bot.GameBot;
import com.antcolonywars.bot.GameBots;
import com.antcolonywars.core.Unit;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.server.GameServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = GameServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GameClientIntegrationTest {
    @LocalServerPort
    private int port;

    @Test
    void connectsMovesSequentiallyAndClosesCleanly() {
        GameClient client = GameClient.connect(serverUrl(), "blue-token");
        assertEquals(1, unitX(client.getLatestState(), "worker-blue"));

        for (int step = 0; step < 3; step++) {
            client.move(Direction.RIGHT);
        }

        assertEquals(4, unitX(client.getLatestState(), "worker-blue"));
        assertEquals(3, client.getLatestState().revision());
        client.close();
        assertTrue(client.isClosed());
    }

    @Test
    void convertsServerRejectionIntoExceptionWithErrorCode() {
        try (GameClient client = GameClient.connect(serverUrl(), "blue-token")) {
            GameClientException exception = assertThrows(
                    GameClientException.class,
                    () -> client.attack("hq-red")
            );
            assertEquals("OUT_OF_RANGE", exception.code());
            assertEquals(0, client.getLatestState().revision());
        }
    }

    @Test
    void rejectsInvalidTokenWithoutWaitingIndefinitely() {
        GameClientException exception = assertThrows(
                GameClientException.class,
                () -> GameClient.connect(serverUrl(), "invalid-token", java.time.Duration.ofSeconds(1))
        );
        assertEquals("CONNECTION_FAILED", exception.code());
    }

    @Test
    void completesWinningScenarioThroughJavaSdkAndWebSocket() {
        try (GameClient client = GameClient.connect(serverUrl(), "blue-token")) {
            for (int step = 0; step < 6; step++) {
                client.move(Direction.DOWN);
            }
            for (int step = 0; step < 6; step++) {
                client.move(Direction.RIGHT);
            }
            for (int hit = 0; hit < 3; hit++) {
                client.attack("hq-red");
            }

            GameState state = client.awaitSimulationIdle();
            assertEquals("FINISHED", state.status());
            assertEquals("BLUE", state.winnerTeamId());
            assertTrue(state.revision() >= 16);
            assertTrue(state.snapshot().simulationTick() >= 1);
            assertEquals(0, state.snapshot().headquarters().stream()
                    .filter(headquarters -> headquarters.id().equals("hq-red"))
                    .findFirst()
                    .orElseThrow()
                    .hp());
        }
    }

    @Test
    void botFacadeRunsWithoutExposingTransportDetails() {
        try (GameBot bot = GameBots.connect(serverUrl(), "blue-token")) {
            for (int step = 0; step < 3; step++) {
                bot.move(Direction.RIGHT);
            }

            assertEquals(4, unitX(bot.getLatestState(), "worker-blue"));
            assertEquals(3, bot.getLatestState().revision());
        }
    }

    @Test
    void botSpawnsSelectsAndMovesAnotherUnit() {
        try (GameBot bot = GameBots.connect(serverUrl(), "blue-token")) {
            bot.spawn("worker-blue-2", "Builder", Direction.DOWN);
            bot.selectUnit("worker-blue-2").move(Direction.RIGHT);

            assertEquals("worker-blue-2", bot.selectedUnitId());
            assertEquals(1, unitX(bot.getLatestState(), "worker-blue-2"));
            assertEquals(1, unitX(bot.getLatestState(), "worker-blue"));
            assertEquals(2, bot.getLatestState().revision());
        }
    }

    @Test
    void botHarvestsAndBuildsFromTheJavaSdk() {
        try (GameBot bot = GameBots.connect(serverUrl(), "blue-token")) {
            bot.move(Direction.RIGHT);
            bot.move(Direction.DOWN);
            bot.move(Direction.DOWN);
            bot.harvest();
            bot.build("barracks-blue", BuildingType.BARRACKS, Direction.RIGHT);

            assertEquals(0, bot.getLatestState().snapshot().teams().stream()
                    .filter(team -> team.id().equals("BLUE"))
                    .findFirst().orElseThrow().resources());
            assertEquals(BuildingType.BARRACKS.name(), bot.getLatestState().snapshot().buildings().getFirst().type().name());
        }
    }

    private String serverUrl() {
        return "ws://localhost:" + port + "/game";
    }

    private int unitX(GameState state, String unitId) {
        return state.snapshot().units().stream()
                .filter(unit -> unit.id().equals(unitId))
                .map(Unit::position)
                .mapToInt(position -> position.x())
                .findFirst()
                .orElseThrow();
    }
}
