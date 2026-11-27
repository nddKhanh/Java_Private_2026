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
        properties = {
                "spring.datasource.url=jdbc:h2:mem:server-integration",
                "game.profile.limits.tick-millis=50"
        }
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
    void queuesACommandAndBroadcastsItsTickToControllerAndViewer() throws Exception {
        SocketClient viewer = connect(null);
        SocketClient blue = connect("blue-token");
        assertEquals(0, viewer.awaitType("STATE_UPDATED").path("revision").asLong());
        JsonNode initial = blue.awaitType("STATE_UPDATED");

        blue.send(command("move-1", "MOVE", Map.of("direction", "RIGHT")));

        JsonNode ack = blue.awaitType("COMMAND_RESULT");
        assertTrue(ack.path("ok").asBoolean());
        assertEquals(1, ack.path("queuedCount").asInt());
        assertEquals(0, ack.path("revision").asLong());
        JsonNode blueState = blue.awaitEvent("move-1");
        JsonNode viewerState = viewer.awaitEvent("move-1");
        assertEquals(1, blueState.path("revision").asLong());
        assertEquals(blueState.path("revision").asLong(), viewerState.path("revision").asLong());
        assertEquals(unitX(initial, "worker-blue") + 1, unitX(viewerState, "worker-blue"));
        assertTrue(viewerState.path("queues").isEmpty());

        viewer.close();
        blue.close();
    }

    @Test
    void reportsOwnershipFailuresOnlyToTheSendingTeam() throws Exception {
        SocketClient viewer = connect(null);
        SocketClient blue = connect("blue-token");
        viewer.awaitType("STATE_UPDATED");
        blue.awaitType("STATE_UPDATED");

        blue.send(command("steal", "WAIT", Map.of("unitId", "worker-red")));
        assertTrue(blue.awaitType("COMMAND_RESULT").path("ok").asBoolean());
        JsonNode rejection = blue.awaitRejection("steal");

        assertEquals("NOT_OWNER", rejection.path("error").asText());
        assertEquals("Quân không thuộc đội của bạn.", rejection.path("errorMessage").asText());
        assertNull(viewer.poll(Duration.ofMillis(300)));

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
    void failedCommandDropsTheRestOfItsBotsBatch() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");

        blue.send(batch("ghost-batch", List.of(
                action("real-wait", "WAIT", Map.of("unitId", "worker-blue")),
                action("ghost-1", "WAIT", Map.of("unitId", "ghost")),
                action("ghost-2", "WAIT", Map.of("unitId", "ghost"))
        )));
        assertEquals(3, blue.awaitType("COMMAND_RESULT").path("queuedCount").asInt());
        JsonNode rejection = blue.awaitRejection("ghost-1");

        assertEquals("UNIT_NOT_FOUND", rejection.path("error").asText());
        assertEquals(1, rejection.path("droppedCount").asInt());
        assertTrue(rejection.path("index").isNull());
        blue.close();
    }

    @Test
    void refusesUnreadableCommandsWithTheirLaterCommandsOnArrival() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");

        blue.send(batch("conversion", List.of(
                action("first", "WAIT", Map.of("unitId", "worker-blue")),
                action("second", "WAIT", Map.of("unitId", "worker-blue")),
                action("bad-direction", "MOVE", Map.of("unitId", "worker-blue", "direction", "SIDEWAYS")),
                action("never-runs", "WAIT", Map.of("unitId", "worker-blue"))
        )));
        JsonNode ack = blue.awaitType("COMMAND_RESULT");

        assertFalse(ack.path("ok").asBoolean());
        assertEquals(2, ack.path("queuedCount").asInt());
        JsonNode rejection = ack.path("rejections").get(0);
        assertEquals("INVALID_COMMAND", rejection.path("error").asText());
        assertEquals(2, rejection.path("index").asInt());
        assertEquals(1, rejection.path("droppedCount").asInt());
        blue.awaitEvent("first");
        assertEquals(2, blue.awaitEvent("second").path("revision").asLong());
        blue.close();
    }

    @Test
    void limitsQueueLengthPerBotAndBatchSize() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");

        blue.send(batch("flood", waits("flood", 41)));
        JsonNode flood = blue.awaitType("COMMAND_RESULT");
        assertEquals("COMMAND_QUEUE_FULL", flood.path("error").asText());
        assertEquals(0, flood.path("queuedCount").asInt());
        assertEquals(40, flood.path("rejections").get(0).path("droppedCount").asInt());

        blue.send(batch("oversized", waits("oversized", 300)));
        assertEquals("INVALID_BATCH", blue.awaitType("COMMAND_RESULT").path("error").asText());
        blue.close();
    }

    @Test
    void clearsTheQueueOfOneOfTheTeamsBots() throws Exception {
        SocketClient blue = connect("blue-token");
        blue.awaitType("STATE_UPDATED");
        blue.send(batch("to-clear", waits("to-clear", 30)));
        blue.awaitType("COMMAND_RESULT");
        JsonNode queued = blue.awaitType("STATE_UPDATED");
        assertEquals("WAIT", queued.path("queues").path("worker-blue").get(0).path("action").asText());

        blue.send(Map.of(
                "type", "CLEAR_QUEUE",
                "schemaVersion", 8,
                "matchId", "match-default",
                "commandId", "clear-1",
                "subjectId", "worker-blue"
        ));
        JsonNode cleared = blue.awaitType("COMMAND_RESULT");

        assertTrue(cleared.path("ok").asBoolean(), cleared.toString());
        assertTrue(cleared.path("queuedCount").asInt() > 0, cleared.toString());
        blue.close();
    }

    private List<Map<String, Object>> waits(String prefix, int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> action(prefix + "-" + index, "WAIT", Map.of("unitId", "worker-blue")))
                .toList();
    }

    @Test
    void runsCommandsOfDifferentTeamsInTheSameTickWithOrderedSequences() throws Exception {
        SocketClient viewer = connect(null);
        SocketClient blue = connect("blue-token");
        SocketClient red = connect("red-token");
        viewer.awaitType("STATE_UPDATED");
        blue.awaitType("STATE_UPDATED");
        red.awaitType("STATE_UPDATED");

        blue.send(command("blue-wait", "WAIT", Map.of()));
        red.send(command("red-wait", "WAIT", Map.of()));
        viewer.awaitEvent("blue-wait");
        viewer.awaitEvent("red-wait");

        assertEquals(List.of(1L, 2L), viewer.seenEventSequences());

        viewer.close();
        blue.close();
        red.close();
    }

    @Test
    void spawnsNamedUnitAndControlsItByGlobalId() throws Exception {
        SocketClient blue = connect("blue-token");
        walkToWoodNode(blue, blue.awaitType("STATE_UPDATED"));
        run(blue, "spawn-harvest", "HARVEST", Map.of());

        JsonNode spawnedState = run(blue, "spawn-blue-2", "SPAWN", Map.of(
                "sourceId", "hq-blue",
                "unitId", "worker-blue-2",
                "displayName", "Builder",
                "direction", "DOWN"
        ));
        JsonNode spawned = unit(spawnedState, "worker-blue-2");
        assertEquals("Builder", spawned.path("displayName").asText());
        assertEquals(0, spawned.path("position").path("x").asInt());
        assertEquals(1, spawned.path("position").path("y").asInt());
        assertEquals(99, teamStock(spawnedState, "BLUE", "meat"));

        JsonNode moved = run(blue, "move-blue-2", "MOVE", Map.of(
                "unitId", "worker-blue-2",
                "direction", "RIGHT"
        ));
        assertEquals(1, unitX(moved, "worker-blue-2"));

        blue.close();
    }

    @Test
    void harvestsAndBuildsWithServerAuthoritativeResources() throws Exception {
        SocketClient blue = connect("blue-token");
        walkToWoodNode(blue, blue.awaitType("STATE_UPDATED"));

        JsonNode harvested = run(blue, "harvest-1", "HARVEST", Map.of());
        assertEquals(103, teamStock(harvested, "BLUE", "wood"));
        assertEquals(47, resourceAmount(harvested, "wood-blue"));

        JsonNode built = run(blue, "build-1", "BUILD", Map.of(
                "buildingId", "barracks-blue",
                "buildingType", "BARRACKS",
                "direction", "RIGHT"
        ));
        assertEquals(100, teamStock(built, "BLUE", "wood"));
        assertEquals("BARRACKS", built.path("state").path("buildings").get(0).path("type").asText());
        blue.close();
    }

    /** Sends one command, checks it was queued and returns the state of the tick that ran it. */
    private JsonNode run(SocketClient client, String commandId, String action, Map<String, Object> extra) throws Exception {
        client.send(command(commandId, action, extra));
        JsonNode ack = client.awaitType("COMMAND_RESULT");
        assertTrue(ack.path("ok").asBoolean(), ack.toString());
        return client.awaitEvent(commandId);
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
        message.put("schemaVersion", 8);
        message.put("matchId", "match-default");
        message.put("commandId", commandId);
        message.put("action", action);
        return message;
    }

    private Map<String, Object> batch(String commandId, List<Map<String, Object>> commands) {
        return Map.of(
                "type", "BATCH",
                "schemaVersion", 8,
                "matchId", "match-default",
                "commandId", commandId,
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
            run(blue, "walk-" + index++, "MOVE", Map.of("direction", direction));
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

        private final List<Long> eventSequences = new java.util.ArrayList<>();
        private final Map<String, JsonNode> statesByEvent = new java.util.HashMap<>();

        /** Waits for the STATE_UPDATED whose events include the command. */
        JsonNode awaitEvent(String commandId) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                if (statesByEvent.containsKey(commandId)) {
                    return statesByEvent.get(commandId);
                }
                JsonNode message = poll(Duration.ofMillis(200));
                if (message == null || !"STATE_UPDATED".equals(message.path("type").asText())) {
                    continue;
                }
                for (JsonNode event : message.path("events")) {
                    eventSequences.add(event.path("sequence").asLong());
                    statesByEvent.put(event.path("commandId").asText(), message);
                }
            }
            throw new AssertionError("Timed out waiting for event of " + commandId);
        }

        /** Waits for the STATE_UPDATED reporting that the command failed on its turn. */
        JsonNode awaitRejection(String commandId) throws Exception {
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                JsonNode message = poll(Duration.ofMillis(200));
                if (message == null) {
                    continue;
                }
                for (JsonNode rejection : message.path("rejections")) {
                    if (commandId.equals(rejection.path("commandId").asText())) {
                        return rejection;
                    }
                }
            }
            throw new AssertionError("Timed out waiting for rejection of " + commandId);
        }

        List<Long> seenEventSequences() {
            return List.copyOf(eventSequences);
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
