package com.antcolonywars.client;

import com.antcolonywars.bot.GameBot;
import com.antcolonywars.bot.GameBots;
import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.GameSession;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.Unit;
import com.antcolonywars.core.WaitCommand;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.server.GameServerApplication;
import com.antcolonywars.server.MatchPersistence;
import com.antcolonywars.server.MatchRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = GameServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:client-integration"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GameClientIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MatchPersistence persistence;

    @Autowired
    private MatchSetup matchSetup;

    @Autowired
    private MatchRegistry matchRegistry;

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
                    () -> client.attack(Direction.UP)
            );
            assertEquals("TARGET_NOT_FOUND", exception.code());
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
    void destroysAnEnemyHeadquartersWithoutEndingTheFactionMatchEarly() {
        try (GameClient client = GameClient.connect(serverUrl(), "blue-token")) {
            for (int step = 0; step < 6; step++) {
                client.move(Direction.RIGHT);
            }
            for (int step = 0; step < 6; step++) {
                client.move(Direction.DOWN);
            }
            for (int hit = 0; hit < 3; hit++) {
                client.attack(Direction.DOWN);
            }

            GameState state = client.awaitSimulationIdle();
            assertEquals("RUNNING", state.status());
            assertEquals(null, state.winnerTeamId());
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
            bot.spawn("worker-blue-2", "Builder", com.antcolonywars.core.UnitType.RANGED, Direction.DOWN);
            bot.selectUnit("worker-blue-2").move(Direction.RIGHT);

            assertEquals("worker-blue-2", bot.selectedUnitId());
            assertEquals("RANGED", bot.getLatestState().snapshot().units().stream()
                    .filter(unit -> unit.id().equals("worker-blue-2"))
                    .findFirst().orElseThrow().type());
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

    @Test
    void accountSessionsJoinTheSameTeamAndPersistWebSocketActions() {
        assertEquals(
                HttpStatus.UNAUTHORIZED,
                restTemplate.getForEntity(apiUrl("/lobby/me"), Map.class).getStatusCode()
        );

        AccountResponse first = register("blue_one", "Blue One");
        SessionResponse firstSession = login(first.username());
        assertThrows(GameClientException.class, () -> GameClient.connect(serverUrl(), firstSession.token()));
        AccountResponse firstMembership = join(firstSession.token(), "BLUE");

        AccountResponse second = register("blue_two", "Blue Two");
        SessionResponse secondSession = login(second.username());
        AccountResponse secondMembership = join(secondSession.token(), "BLUE");

        assertEquals("BLUE", firstMembership.teamId());
        assertEquals("BLUE", secondMembership.teamId());

        try (GameClient firstClient = GameClient.connect(serverUrl(), firstSession.token());
             GameClient secondClient = GameClient.connect(serverUrl(), secondSession.token())) {
            firstClient.move(Direction.RIGHT);
            assertEquals(3, unitX(secondClient.move(Direction.RIGHT), "worker-blue"));
        }

        var persistedState = persistence.load("match-default").orElseThrow();
        assertEquals(2, persistedState.revision());
        assertEquals(2, persistence.history("match-default").size());
        assertEquals("MOVE", persistence.history("match-default").getFirst().action());

        GameSession restored = new GameSession(matchSetup, persistedState);
        assertTrue(restored.apply(new WaitCommand(
                "wait-after-jdbc-restore", "BLUE", "worker-blue"
        )).accepted());
        assertEquals(3, restored.state().revision());

        ResponseEntity<GameEvent[]> historyResponse = restTemplate.getForEntity(
                apiUrl("/matches/match-default/history"), GameEvent[].class
        );
        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());
        assertEquals(2, historyResponse.getBody().length);
    }

    @Test
    void enforcesFiveAccountsPerTeam() {
        java.util.ArrayList<SessionResponse> controllers = new java.util.ArrayList<>();
        for (int index = 1; index <= 5; index++) {
            AccountResponse account = register("team_user_" + index, "Team User " + index);
            SessionResponse session = login(account.username());
            assertEquals("CYAN", join(session.token(), "CYAN").teamId());
            controllers.add(session);
        }

        java.util.ArrayList<GameClient> clients = new java.util.ArrayList<>();
        try {
            for (SessionResponse controller : controllers) {
                clients.add(GameClient.connect(serverUrl(), controller.token()));
            }
            int expectedRevision = 0;
            for (GameClient client : clients) {
                assertEquals(++expectedRevision, client.waitTurn("worker-cyan").revision());
            }
            assertEquals(5, matchRegistry.require("match-default").state().revision());
        } finally {
            clients.forEach(GameClient::close);
        }

        AccountResponse overflow = register("team_user_6", "Team User 6");
        SessionResponse overflowSession = login(overflow.username());
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(overflowSession.token());
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl("/lobby/membership"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("teamId", "CYAN"), headers),
                Map.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("TEAM_FULL", response.getBody().get("code"));
    }

    @Test
    void reconnectsToTheLatestSnapshotAndKeepsMatchesIsolated() {
        try (GameClient first = GameClient.connect(serverUrl(), "blue-token")) {
            first.move(Direction.RIGHT);
        }

        try (GameClient reconnected = GameClient.connect(serverUrl(), "blue-token")) {
            assertEquals(2, unitX(reconnected.getLatestState(), "worker-blue"));
            assertEquals(1, reconnected.getLatestState().revision());
        }

        try (GameClient training = GameClient.connect(serverUrl(), "blue-token", "match-training")) {
            assertEquals("match-training", training.getLatestState().snapshot().matchId());
            assertEquals(1, unitX(training.getLatestState(), "worker-blue"));
            training.move(Direction.RIGHT);
        }

        assertEquals(1, matchRegistry.require("match-default").state().revision());
        assertEquals(1, matchRegistry.require("match-training").state().revision());
    }

    @Test
    void exposesTheFullScaleProfile() {
        assertEquals(10, matchSetup.teams().size());
        assertEquals(2, matchSetup.teams().stream().map(team -> team.factionId()).distinct().count());
        assertEquals(50, matchSetup.rules().maxUnitsPerTeam());
        assertEquals(2, matchRegistry.runtimes().size());
    }

    private String serverUrl() {
        return "ws://localhost:" + port + "/game";
    }

    private AccountResponse register(String username, String displayName) {
        ResponseEntity<AccountResponse> response = restTemplate.postForEntity(
                apiUrl("/accounts"),
                Map.of("username", username, "displayName", displayName, "password", "strong-pass-123"),
                AccountResponse.class
        );
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return response.getBody();
    }

    private SessionResponse login(String username) {
        ResponseEntity<SessionResponse> response = restTemplate.postForEntity(
                apiUrl("/sessions"),
                Map.of("username", username, "password", "strong-pass-123"),
                SessionResponse.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private AccountResponse join(String token, String teamId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<AccountResponse> response = restTemplate.exchange(
                apiUrl("/lobby/membership"),
                HttpMethod.PUT,
                new HttpEntity<>(Map.of("teamId", teamId), headers),
                AccountResponse.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private String apiUrl(String path) {
        return "http://localhost:" + port + "/api" + path;
    }

    private record AccountResponse(String id, String username, String displayName, String teamId) {
    }

    private record SessionResponse(String token, String expiresAt, AccountResponse account) {
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
