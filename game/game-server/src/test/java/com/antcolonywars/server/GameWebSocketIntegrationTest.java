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
import java.util.List;
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

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:server-integration"
)
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
    void sendsTeamStockOnlyToItsOwnTeamEvenAmongAllies() throws Exception {
        SocketClient viewer = connect(null);
        SocketClient blue = connect("blue-token");
        JsonNode viewerTeams = viewer.awaitType("STATE_UPDATED").path("state").path("teams");
        JsonNode blueTeams = blue.awaitType("STATE_UPDATED").path("state").path("teams");

        for (JsonNode team : blueTeams) {
            boolean own = team.path("id").asText().equals("BLUE");
            assertEquals(own, team.has("wood"), team.path("id").asText());
            assertEquals(own, team.has("meat"), team.path("id").asText());
        }
        for (JsonNode team : viewerTeams) {
            assertFalse(team.has("wood") || team.has("meat"));
        }

        viewer.close();
        blue.close();
    }

    @Test
    void broadcastsAcceptedMoveToControllerAndViewerAtSameRevision() throws Exception {
        SocketClient viewer = connect(null);
        SocketClient blue = connect("blue-token");
        assertEquals(0, viewer.awaitType("STATE_UPDATED").path("revision").asLong());
        JsonNode initial = blue.awaitType("STATE_UPDATED");
        assertEquals(0, initial.path("revision").asLong());

        blue.send(command("move-1", "MOVE", Map.of("direction", "RIGHT")));

        JsonNode result = blue.awaitType("COMMAND_RESULT");
        JsonNode blueState = blue.awaitType("STATE_UPDATED");
        JsonNode viewerState = viewer.awaitType("STATE_UPDATED");
        assertTrue(result.path("ok").asBoolean());
        assertEquals("move-1", result.path("commandId").asText());
        assertEquals(1, result.path("revision").asLong());
        assertEquals(1, blueState.path("revision").asLong());
        assertEquals(blueState.path("revision").asLong(), viewerState.path("revision").asLong());
        assertEquals(unitX(initial, "worker-blue") + 1, unitX(viewerState, "worker-blue"));

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
    void appliesAtomicAndPartialBatchesWithVietnameseErrors() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");

        blue.send(batch("atomic-batch", "ATOMIC", List.of(
                action("atomic-wait", "WAIT", Map.of("unitId", "worker-blue")),
                action("atomic-steal", "WAIT", Map.of("unitId", "worker-red"))
        )));
        JsonNode atomic = blue.awaitType("COMMAND_RESULT");
        assertFalse(atomic.path("ok").asBoolean());
        assertEquals("NOT_OWNER", atomic.path("error").asText());
        assertEquals("Quân không thuộc đội của bạn.", atomic.path("errorMessage").asText());
        assertEquals(0, atomic.path("appliedCount").asInt());
        assertEquals(0, atomic.path("revision").asLong());

        blue.send(batch("partial-batch", "PARTIAL", List.of(
                action("partial-wait", "WAIT", Map.of("unitId", "worker-blue")),
                action("partial-steal", "WAIT", Map.of("unitId", "worker-red"))
        )));
        JsonNode partial = blue.awaitType("COMMAND_RESULT");
        assertFalse(partial.path("ok").asBoolean());
        assertEquals("NOT_OWNER", partial.path("error").asText());
        assertEquals(1, partial.path("failedIndex").asInt());
        assertEquals(1, partial.path("appliedCount").asInt());
        assertEquals(1, blue.awaitType("STATE_UPDATED").path("revision").asLong());
        blue.close();
    }

    @Test
    void appliesConversionFailuresAtTheirSequentialBatchPosition() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");
        List<Map<String, Object>> commands = List.of(
                action("first", "WAIT", Map.of("unitId", "worker-blue")),
                action("second", "WAIT", Map.of("unitId", "worker-blue")),
                action("bad-direction", "MOVE", Map.of("unitId", "worker-blue", "direction", "SIDEWAYS")),
                action("never-runs", "WAIT", Map.of("unitId", "worker-blue"))
        );

        blue.send(batch("atomic-conversion", "ATOMIC", commands));
        JsonNode atomic = blue.awaitType("COMMAND_RESULT");
        assertEquals("INVALID_COMMAND", atomic.path("error").asText());
        assertEquals(2, atomic.path("failedIndex").asInt());
        assertEquals(0, atomic.path("appliedCount").asInt());
        assertEquals(0, atomic.path("revision").asLong());

        blue.send(batch("partial-conversion", "PARTIAL", commands));
        JsonNode partial = blue.awaitType("COMMAND_RESULT");
        assertEquals("INVALID_COMMAND", partial.path("error").asText());
        assertEquals(2, partial.path("failedIndex").asInt());
        assertEquals(2, partial.path("appliedCount").asInt());
        assertEquals(2, partial.path("revision").asLong());
        JsonNode partialState = blue.awaitType("STATE_UPDATED");
        assertEquals(2, partialState.path("revision").asLong());
        assertEquals(2, partialState.path("events").size());
        assertEquals("first", partialState.path("events").get(0).path("commandId").asText());
        assertEquals("second", partialState.path("events").get(1).path("commandId").asText());
        blue.close();
    }

    @Test
    void acceptsLargeBatchesUpToTheProfileLimit() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");

        blue.send(batch("large-batch", "PARTIAL", waits("large", 300)));
        JsonNode large = blue.awaitType("COMMAND_RESULT");
        assertEquals("BATCH_LIMIT_EXCEEDED", large.path("error").asText());
        assertEquals(10, large.path("failedIndex").asInt());
        assertEquals(10, large.path("appliedCount").asInt());
        assertEquals(10, blue.awaitType("STATE_UPDATED").path("events").size());

        blue.send(batch("oversized-batch", "PARTIAL", waits("oversized", 2000)));
        assertEquals("INVALID_BATCH", blue.awaitType("COMMAND_RESULT").path("error").asText());
        blue.close();
    }

    private List<Map<String, Object>> waits(String prefix, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> action(prefix + "-" + index, "WAIT", Map.of("unitId", "worker-blue")))
                .toList();
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
        assertNotNull(first.path("events").get(0).path("commandId").textValue());
        assertNotNull(second.path("events").get(0).path("commandId").textValue());

        viewer.close();
        blue.close();
        red.close();
    }

    @Test
    void spawnsNamedUnitAndControlsItByGlobalId() throws Exception {
        SocketClient blue = connect("blue-token");
        walkToWoodNode(blue, blue.awaitType("STATE_UPDATED"));
        blue.send(command("spawn-harvest", "HARVEST", Map.of()));
        assertTrue(blue.awaitType("COMMAND_RESULT").path("ok").asBoolean());
        blue.awaitType("STATE_UPDATED");

        blue.send(command("spawn-blue-2", "SPAWN", Map.of(
                "sourceId", "hq-blue",
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
        assertEquals(99, teamStock(spawnedState, "BLUE", "meat"));

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
        walkToWoodNode(blue, blue.awaitType("STATE_UPDATED"));

        blue.send(command("harvest-1", "HARVEST", Map.of()));
        assertTrue(blue.awaitType("COMMAND_RESULT").path("ok").asBoolean());
        JsonNode harvested = blue.awaitType("STATE_UPDATED");
        assertEquals(103, teamStock(harvested, "BLUE", "wood"));
        assertEquals(47, resourceAmount(harvested, "wood-blue"));

        blue.send(command("build-1", "BUILD", Map.of(
                "buildingId", "barracks-blue",
                "buildingType", "BARRACKS",
                "direction", "RIGHT"
        )));
        assertTrue(blue.awaitType("COMMAND_RESULT").path("ok").asBoolean());
        JsonNode built = blue.awaitType("STATE_UPDATED");
        assertEquals(100, teamStock(built, "BLUE", "wood"));
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
        message.put("schemaVersion", 7);
        message.put("matchId", "match-default");
        message.put("commandId", commandId);
        message.put("action", action);
        return message;
    }

    private Map<String, Object> batch(String commandId, String mode, List<Map<String, Object>> commands) {
        return Map.of(
                "type", "BATCH",
                "schemaVersion", 7,
                "matchId", "match-default",
                "commandId", commandId,
                "mode", mode,
                "commands", commands
        );
    }

    private Map<String, Object> action(String commandId, String action, Map<String, Object> extra) {
        Map<String, Object> message = new java.util.HashMap<>(extra);
        message.put("commandId", commandId);
        message.put("action", action);
        return message;
    }

    /** The starter worker spawns next to hq-blue (0, 0); walking right then down reaches wood-blue at (2, 2). */
    private void walkToWoodNode(SocketClient blue, JsonNode state) throws Exception {
        JsonNode position = unit(state, "worker-blue").path("position");
        List<String> steps = new java.util.ArrayList<>();
        for (int x = position.path("x").asInt(); x < 2; x++) steps.add("RIGHT");
        for (int y = position.path("y").asInt(); y < 2; y++) steps.add("DOWN");
        int index = 0;
        for (String direction : steps) {
            blue.send(command("walk-" + index++, "MOVE", Map.of("direction", direction)));
            JsonNode moveResult = blue.awaitType("COMMAND_RESULT");
            assertTrue(moveResult.path("ok").asBoolean(), moveResult.toString());
            blue.awaitType("STATE_UPDATED");
        }
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

    private int teamStock(JsonNode envelope, String teamId, String resource) {
        for (JsonNode team : envelope.path("state").path("teams")) {
            if (teamId.equals(team.path("id").asText())) {
                return team.path(resource).asInt();
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
