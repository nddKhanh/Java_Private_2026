package com.antcolonywars.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:lobby-flow"
)
@DirtiesContext
class LobbyFlowIntegrationTest {
    private static final String MAP_SCRIPT = """
            {
              "map": "8x8",
              "teams": 2,
              "accountsPerTeam": 1,
              "resources": [{"x": 3, "y": 3, "amount": 10}],
              "headquarters": [
                {"team": 1, "x": 0, "y": 0},
                {"team": 2, "x": 7, "y": 7}
              ],
              "units": [
                {"team": 1, "x": 1, "y": 0, "type": "WORKER"},
                {"team": 2, "x": 6, "y": 7, "type": "WORKER"}
              ]
            }
            """;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MatchRegistry matchRegistry;

    @Autowired
    private LobbyService lobbyService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registersCreatesJoinsEnforcesCapacityAndStartsDynamicMatch() throws Exception {
        String ownerToken = registerAndLogin("owner_one", "x");
        String secondToken = registerAndLogin("second_one", "y");
        String thirdToken = registerAndLogin("third_one", "z");

        ResponseEntity<String> created = exchange(
                "/api/lobbies", HttpMethod.POST, ownerToken, Map.of("mapScript", MAP_SCRIPT)
        );
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        JsonNode lobby = objectMapper.readTree(created.getBody());
        String matchCode = lobby.path("matchCode").asText();
        String pairingKey = lobby.path("pairingKey").asText();
        String teamOneToken = lobby.path("teams").get(0).path("token").asText();
        String teamTwoToken = lobby.path("teams").get(1).path("token").asText();
        assertEquals(6, matchCode.length());
        assertEquals(6, pairingKey.length());
        assertEquals("BLUE", teamOneToken);
        assertEquals("RED", teamTwoToken);
        assertEquals(1, lobby.path("teams").get(0).path("members").size());

        ResponseEntity<String> paired = restTemplate.getForEntity(
                "/api/desktop/pairing/" + pairingKey, String.class
        );
        assertEquals(HttpStatus.OK, paired.getStatusCode());
        JsonNode pairing = objectMapper.readTree(paired.getBody());
        assertEquals(matchCode, pairing.path("matchCode").asText());
        assertTrue(pairing.path("serverUrl").asText().endsWith("/game"));
        assertEquals("BLUE", pairing.path("teams").get(0).path("token").asText());

        ResponseEntity<String> joined = exchange(
                "/api/lobbies/join", HttpMethod.POST, secondToken,
                Map.of("matchCode", matchCode, "teamToken", teamTwoToken)
        );
        assertEquals(HttpStatus.OK, joined.getStatusCode());
        assertEquals(1, objectMapper.readTree(joined.getBody()).path("teams").get(1).path("members").size());
        assertEquals("TEAM_2", lobbyService.resolveTeamId(matchCode, teamTwoToken));
        assertEquals("TEAM_2", lobbyService.resolveTeamId(matchCode, secondToken));

        ResponseEntity<String> full = exchange(
                "/api/lobbies/join", HttpMethod.POST, thirdToken,
                Map.of("matchCode", matchCode, "teamToken", teamOneToken)
        );
        assertEquals(HttpStatus.CONFLICT, full.getStatusCode());
        assertEquals("TEAM_FULL", objectMapper.readTree(full.getBody()).path("code").asText());

        ResponseEntity<String> started = exchange(
                "/api/lobbies/" + matchCode + "/start", HttpMethod.POST, ownerToken, null
        );
        assertEquals(HttpStatus.OK, started.getStatusCode());
        assertEquals("RUNNING", objectMapper.readTree(started.getBody()).path("status").asText());
        assertTrue(matchRegistry.find(matchCode).isPresent());
    }

    private String registerAndLogin(String username, String password) throws Exception {
        ResponseEntity<String> registered = restTemplate.postForEntity(
                "/api/accounts", Map.of("username", username, "password", password), String.class
        );
        assertEquals(HttpStatus.CREATED, registered.getStatusCode());
        ResponseEntity<String> loggedIn = restTemplate.postForEntity(
                "/api/sessions", Map.of("username", username, "password", password), String.class
        );
        assertEquals(HttpStatus.OK, loggedIn.getStatusCode());
        return objectMapper.readTree(loggedIn.getBody()).path("token").asText();
    }

    private ResponseEntity<String> exchange(
            String path, HttpMethod method, String token, Object body
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), String.class);
    }
}
