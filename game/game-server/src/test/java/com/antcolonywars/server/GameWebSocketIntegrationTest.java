package com.antcolonywars.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class GameWebSocketIntegrationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @LocalServerPort
    private int port;

    @Test
    void rejectsInvalidTokenDuringHandshake() {
        assertThrows(CompletionException.class, () -> connect("wrong-token"));
    }

    @Test
    void broadcastsAcceptedMoveToControllerAndViewerAtSameRevision() throws Exception {
        SocketClient viewer = connect(null);
        SocketClient blue = connect("blue-token");
        assertEquals(0, viewer.awaitType("STATE_UPDATED").path("revision").asLong());
        assertEquals(0, blue.awaitType("STATE_UPDATED").path("revision").asLong());

        blue.send(command("move-1", "MOVE", Map.of("direction", "RIGHT")));

        JsonNode result = blue.awaitType("COMMAND_RESULT");
        JsonNode blueState = blue.awaitType("STATE_UPDATED");
        JsonNode viewerState = viewer.awaitType("STATE_UPDATED");
        assertTrue(result.path("ok").asBoolean());
        assertEquals("move-1", result.path("commandId").asText());
        assertEquals(1, result.path("revision").asLong());
        assertEquals(1, blueState.path("revision").asLong());
        assertEquals(blueState.path("revision").asLong(), viewerState.path("revision").asLong());
        assertEquals(2, unitX(viewerState, "worker-blue"));

        viewer.close();
        blue.close();
    }

    @Test
    void reportsOwnershipAndSchemaErrorsWithoutBroadcastingFakeState() throws Exception {
        SocketClient viewer = connect(null);
        SocketClient blue = connect("blue-token");
        viewer.awaitType("STATE_UPDATED");
        blue.awaitType("STATE_UPDATED");

        blue.send(command("steal", "WAIT", Map.of("unitId", "worker-red")));
        JsonNode ownershipError = blue.awaitType("COMMAND_RESULT");

        assertFalse(ownershipError.path("ok").asBoolean());
        assertEquals("NOT_OWNER", ownershipError.path("error").asText());
        assertNull(viewer.poll(Duration.ofMillis(250)));

        blue.send(Map.of(
                "type", "COMMAND",
                "schemaVersion", 0,
                "matchId", "match-default",
                "commandId", "old-client",
                "action", "WAIT"
        ));
        JsonNode schemaError = blue.awaitType("COMMAND_RESULT");
        assertEquals("UNSUPPORTED_SCHEMA_VERSION", schemaError.path("error").asText());

        viewer.close();
        blue.close();
    }

    @Test
    void serializesNearConcurrentCommandsIntoUniqueOrderedSequences() throws Exception {
        SocketClient viewer = connect(null);
        SocketClient blue = connect("blue-token");
        SocketClient red = connect("red-token");
        viewer.awaitType("STATE_UPDATED");
        blue.awaitType("STATE_UPDATED");
        red.awaitType("STATE_UPDATED");

        blue.send(command("blue-wait", "WAIT", Map.of()));
        red.send(command("red-wait", "WAIT", Map.of()));

        JsonNode first = viewer.awaitType("STATE_UPDATED");
        JsonNode second = viewer.awaitType("STATE_UPDATED");
        assertEquals(1, first.path("sequence").asLong());
        assertEquals(2, second.path("sequence").asLong());
        assertNotNull(first.path("event").path("commandId").textValue());
        assertNotNull(second.path("event").path("commandId").textValue());

        viewer.close();
        blue.close();
        red.close();
    }

    @Test
    void spawnsNamedUnitAndControlsItByGlobalId() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");

        blue.send(command("spawn-blue-2", "SPAWN", Map.of(
                "unitId", "worker-blue-2",
                "displayName", "Builder",
                "direction", "DOWN"
        )));
        assertTrue(blue.awaitType("COMMAND_RESULT").path("ok").asBoolean());
        JsonNode spawnedState = blue.awaitType("STATE_UPDATED");
        JsonNode spawned = unit(spawnedState, "worker-blue-2");
        assertEquals("Builder", spawned.path("displayName").asText());
        assertEquals(0, spawned.path("position").path("x").asInt());
        assertEquals(1, spawned.path("position").path("y").asInt());

        blue.send(command("move-blue-2", "MOVE", Map.of(
                "unitId", "worker-blue-2",
                "direction", "RIGHT"
        )));
        assertTrue(blue.awaitType("COMMAND_RESULT").path("ok").asBoolean());
        assertEquals(1, unitX(blue.awaitType("STATE_UPDATED"), "worker-blue-2"));

        blue.close();
    }

    @Test
    void harvestsAndBuildsWithServerAuthoritativeResources() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");
        int moveIndex = 0;
        for (String direction : new String[]{"RIGHT", "DOWN", "DOWN"}) {
            blue.send(command("move-ore-" + moveIndex++, "MOVE", Map.of("direction", direction)));
            JsonNode moveResult = blue.awaitType("COMMAND_RESULT");
            assertTrue(moveResult.path("ok").asBoolean(), moveResult.toString());
            blue.awaitType("STATE_UPDATED");
        }

        blue.send(command("harvest-1", "HARVEST", Map.of()));
        assertTrue(blue.awaitType("COMMAND_RESULT").path("ok").asBoolean());
        JsonNode harvested = blue.awaitType("STATE_UPDATED");
        assertEquals(3, teamResources(harvested, "BLUE"));
        assertEquals(7, resourceAmount(harvested, "ore-center"));

        blue.send(command("build-1", "BUILD", Map.of(
                "buildingId", "barracks-blue",
                "buildingType", "BARRACKS",
                "direction", "RIGHT"
        )));
        assertTrue(blue.awaitType("COMMAND_RESULT").path("ok").asBoolean());
        JsonNode built = blue.awaitType("STATE_UPDATED");
        assertEquals(0, teamResources(built, "BLUE"));
        assertEquals("BARRACKS", built.path("state").path("buildings").get(0).path("type").asText());
        blue.close();
    }

    private SocketClient connect(String token) {
        String query = token == null ? "" : "?token=" + token;
        SocketClient listener = new SocketClient(objectMapper);
        WebSocket webSocket = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build()
                .newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .buildAsync(URI.create("ws://localhost:" + port + "/game" + query), listener)
                .join();
        listener.attach(webSocket);
        return listener;
    }

    private Map<String, Object> command(String commandId, String action, Map<String, Object> extra) {
        Map<String, Object> message = new java.util.HashMap<>(extra);
        message.put("type", "COMMAND");
        message.put("schemaVersion", 4);
        message.put("matchId", "match-default");
        message.put("commandId", commandId);
        message.put("action", action);
        return message;
    }

    private int unitX(JsonNode envelope, String unitId) {
        return unit(envelope, unitId).path("position").path("x").asInt();
    }

    private JsonNode unit(JsonNode envelope, String unitId) {
        for (JsonNode unit : envelope.path("state").path("units")) {
            if (unitId.equals(unit.path("id").asText())) {
                return unit;
            }
        }
        throw new AssertionError("Unit not found: " + unitId);
    }

    private int teamResources(JsonNode envelope, String teamId) {
        for (JsonNode team : envelope.path("state").path("teams")) {
            if (teamId.equals(team.path("id").asText())) {
                return team.path("resources").asInt();
            }
        }
        throw new AssertionError("Team not found: " + teamId);
    }

    private int resourceAmount(JsonNode envelope, String resourceId) {
        for (JsonNode resource : envelope.path("state").path("resourceNodes")) {
            if (resourceId.equals(resource.path("id").asText())) {
                return resource.path("amount").asInt();
            }
        }
        throw new AssertionError("Resource not found: " + resourceId);
    }

    private static final class SocketClient implements WebSocket.Listener {
        private final ObjectMapper objectMapper;
        private final LinkedBlockingQueue<String> messages = new LinkedBlockingQueue<>();
        private final StringBuilder fragments = new StringBuilder();
        private WebSocket webSocket;

        private SocketClient(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

        void attach(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        void send(Object message) throws Exception {
            webSocket.sendText(objectMapper.writeValueAsString(message), true).join();
        }

        JsonNode awaitType(String type) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
            while (System.nanoTime() < deadline) {
                JsonNode message = poll(Duration.ofMillis(200));
                if (message != null && type.equals(message.path("type").asText())) {
                    return message;
                }
            }
            throw new AssertionError("Timed out waiting for " + type);
        }

        JsonNode poll(Duration timeout) throws Exception {
            String message = messages.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return message == null ? null : objectMapper.readTree(message);
        }

        void close() {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "done").join();
        }

        @Override
        public void onOpen(WebSocket webSocket) {
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            fragments.append(data);
            if (last) {
                messages.add(fragments.toString());
                fragments.setLength(0);
            }
            webSocket.request(1);
            return null;
        }
    }
}
