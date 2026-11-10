package com.antcolonywars.client;

import com.antcolonywars.core.MatchState;
import com.antcolonywars.core.UnitType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class GameClient implements AutoCloseable, WebSocket.Listener {
    private static final int SCHEMA_VERSION = 5;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Duration timeout;
    private final Map<String, CompletableFuture<CommandResult>> pending = new ConcurrentHashMap<>();
    private final Object stateMonitor = new Object();
    private final StringBuilder textFragments = new StringBuilder();
    private volatile MatchState latestState;
    private volatile Throwable connectionFailure;
    private volatile boolean closed;
    private WebSocket webSocket;

    private GameClient(Duration timeout) {
        this.timeout = timeout;
    }

    public static GameClient connect(String url, String teamToken) {
        return connect(url, teamToken, DEFAULT_TIMEOUT);
    }

    public static GameClient connect(String url, String teamToken, Duration timeout) {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(teamToken, "teamToken");
        Objects.requireNonNull(timeout, "timeout");
        GameClient client = new GameClient(timeout);
        client.open(withToken(url, teamToken));
        return client;
    }

    public GameState move(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return sendAction("MOVE", Map.of("direction", direction.name()));
    }

    public GameState move(String unitId, Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return sendUnitAction("MOVE", unitId, Map.of("direction", direction.name()));
    }

    public GameState waitTurn() {
        return sendAction("WAIT", Map.of());
    }

    public GameState waitTurn(String unitId) {
        return sendUnitAction("WAIT", unitId, Map.of());
    }

    public GameState attack(String targetId) {
        Objects.requireNonNull(targetId, "targetId");
        return sendAction("ATTACK", Map.of("targetId", targetId));
    }

    public GameState attack(String unitId, String targetId) {
        Objects.requireNonNull(targetId, "targetId");
        return sendUnitAction("ATTACK", unitId, Map.of("targetId", targetId));
    }

    public GameState spawn(String unitId, String displayName, Direction direction) {
        return spawn(unitId, displayName, UnitType.WORKER, direction);
    }

    public GameState spawn(String unitId, String displayName, UnitType unitType, Direction direction) {
        Objects.requireNonNull(unitId, "unitId");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(unitType, "unitType");
        Objects.requireNonNull(direction, "direction");
        return sendAction("SPAWN", Map.of(
                "unitId", unitId,
                "displayName", displayName,
                "unitType", unitType.name(),
                "direction", direction.name()
        ));
    }

    public GameState harvest() {
        return sendAction("HARVEST", Map.of());
    }

    public GameState harvest(String unitId) {
        return sendUnitAction("HARVEST", unitId, Map.of());
    }

    public GameState build(String buildingId, BuildingType buildingType, Direction direction) {
        Objects.requireNonNull(buildingId, "buildingId");
        Objects.requireNonNull(buildingType, "buildingType");
        Objects.requireNonNull(direction, "direction");
        return sendAction("BUILD", Map.of(
                "buildingId", buildingId,
                "buildingType", buildingType.name(),
                "direction", direction.name()
        ));
    }

    public GameState build(String unitId, String buildingId, BuildingType buildingType, Direction direction) {
        Objects.requireNonNull(buildingType, "buildingType");
        Objects.requireNonNull(direction, "direction");
        return sendUnitAction("BUILD", unitId, Map.of(
                "buildingId", buildingId,
                "buildingType", buildingType.name(),
                "direction", direction.name()
        ));
    }

    public GameState getLatestState() {
        MatchState state = latestState;
        if (state == null) {
            throw new GameClientException("NO_STATE", "The server has not sent a state snapshot");
        }
        return new GameState(state);
    }

    public GameState awaitSimulationIdle() {
        return awaitSimulationIdle(timeout);
    }

    public GameState awaitSimulationIdle(Duration maximumWait) {
        Objects.requireNonNull(maximumWait, "maximumWait");
        ensureOpen();
        long deadline = System.nanoTime() + maximumWait.toNanos();
        synchronized (stateMonitor) {
            while (latestState == null || !latestState.projectiles().isEmpty()) {
                if (connectionFailure != null) {
                    throw new GameClientException("CONNECTION_ERROR", "Connection failed", connectionFailure);
                }
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    throw new GameClientException("TIMEOUT", "Timed out waiting for simulation to become idle");
                }
                try {
                    TimeUnit.NANOSECONDS.timedWait(stateMonitor, remaining);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new GameClientException("INTERRUPTED", "Interrupted while waiting for simulation", exception);
                }
            }
        }
        return getLatestState();
    }

    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (webSocket != null) {
            try {
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "client closing")
                        .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new GameClientException("INTERRUPTED", "Interrupted while closing WebSocket", exception);
            } catch (ExecutionException | TimeoutException exception) {
                webSocket.abort();
                throw new GameClientException("CLOSE_FAILED", "WebSocket did not close cleanly", exception);
            }
        }
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        synchronized (textFragments) {
            textFragments.append(data);
            if (last) {
                handleMessage(textFragments.toString());
                textFragments.setLength(0);
            }
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        closed = true;
        failPending(new GameClientException("CONNECTION_CLOSED", "WebSocket closed: " + reason));
        return null;
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        recordConnectionFailure(new GameClientException("CONNECTION_ERROR", "WebSocket transport failed", error));
    }

    private void open(String authenticatedUrl) {
        try {
            webSocket = HttpClient.newBuilder()
                    .connectTimeout(timeout)
                    .build()
                    .newWebSocketBuilder()
                    .connectTimeout(timeout)
                    .buildAsync(URI.create(authenticatedUrl), this)
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            waitForRevision(0, true);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GameClientException("INTERRUPTED", "Interrupted while connecting", exception);
        } catch (ExecutionException | TimeoutException | IllegalArgumentException exception) {
            throw new GameClientException("CONNECTION_FAILED", "Cannot connect to game server", exception);
        }
    }

    private GameState sendAction(String action, Map<String, Object> parameters) {
        ensureOpen();
        String commandId = UUID.randomUUID().toString();
        Map<String, Object> message = new HashMap<>(parameters);
        message.put("type", "COMMAND");
        message.put("schemaVersion", SCHEMA_VERSION);
        message.put("matchId", latestState.matchId());
        message.put("commandId", commandId);
        message.put("action", action);

        CompletableFuture<CommandResult> response = new CompletableFuture<>();
        pending.put(commandId, response);
        try {
            webSocket.sendText(objectMapper.writeValueAsString(message), true)
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            CommandResult result = response.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!result.ok()) {
                if ("RATE_LIMITED".equals(result.error())) {
                    waitForRateWindow();
                    return sendAction(action, parameters);
                }
                throw new GameClientException(result.error(), "Command rejected: " + result.error());
            }
            waitForRevision(result.revision(), false);
            return getLatestState();
        } catch (GameClientException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GameClientException("INTERRUPTED", "Interrupted while waiting for command", exception);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof GameClientException clientException) {
                throw clientException;
            }
            throw new GameClientException("COMMAND_FAILED", "Cannot send command", exception.getCause());
        } catch (TimeoutException exception) {
            throw new GameClientException("TIMEOUT", "Timed out waiting for command " + commandId, exception);
        } catch (Exception exception) {
            throw new GameClientException("INVALID_MESSAGE", "Cannot encode command", exception);
        } finally {
            pending.remove(commandId);
        }
    }

    private void waitForRateWindow() {
        try {
            Thread.sleep(1_050);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GameClientException("INTERRUPTED", "Interrupted while waiting for rate limit", exception);
        }
    }

    private GameState sendUnitAction(String action, String unitId, Map<String, Object> parameters) {
        Objects.requireNonNull(unitId, "unitId");
        if (unitId.isBlank()) {
            throw new IllegalArgumentException("unitId must not be blank");
        }
        Map<String, Object> message = new HashMap<>(parameters);
        message.put("unitId", unitId);
        return sendAction(action, message);
    }

    private void handleMessage(String payload) {
        try {
            JsonNode message = objectMapper.readTree(payload);
            int schemaVersion = message.path("schemaVersion").asInt(-1);
            if (schemaVersion != SCHEMA_VERSION) {
                throw new GameClientException(
                        "UNSUPPORTED_SCHEMA_VERSION",
                        "Server schema " + schemaVersion + " is not supported"
                );
            }
            String type = message.path("type").asText();
            if ("STATE_UPDATED".equals(type)) {
                MatchState state = objectMapper.treeToValue(message.path("state"), MatchState.class);
                latestState = state;
                synchronized (stateMonitor) {
                    stateMonitor.notifyAll();
                }
                return;
            }
            if ("COMMAND_RESULT".equals(type)) {
                String commandId = message.path("commandId").isNull()
                        ? null
                        : message.path("commandId").asText();
                CompletableFuture<CommandResult> future = commandId == null ? null : pending.get(commandId);
                if (future != null) {
                    future.complete(new CommandResult(
                            commandId,
                            message.path("ok").asBoolean(),
                            message.path("error").isNull() ? null : message.path("error").asText(),
                            message.path("revision").asLong(),
                            message.path("sequence").asLong()
                    ));
                }
            }
        } catch (GameClientException exception) {
            recordConnectionFailure(exception);
        } catch (Exception exception) {
            recordConnectionFailure(new GameClientException(
                    "INVALID_SERVER_MESSAGE",
                    "Cannot decode server message",
                    exception
            ));
        }
    }

    private void waitForRevision(long revision, boolean allowAnyInitialRevision) {
        long deadline = System.nanoTime() + timeout.toNanos();
        synchronized (stateMonitor) {
            while (latestState == null || (!allowAnyInitialRevision && latestState.revision() < revision)) {
                if (connectionFailure != null) {
                    if (connectionFailure instanceof GameClientException clientException) {
                        throw clientException;
                    }
                    throw new GameClientException("CONNECTION_ERROR", "Connection failed", connectionFailure);
                }
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    throw new GameClientException("TIMEOUT", "Timed out waiting for state revision " + revision);
                }
                try {
                    TimeUnit.NANOSECONDS.timedWait(stateMonitor, remaining);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new GameClientException("INTERRUPTED", "Interrupted while waiting for state", exception);
                }
            }
        }
    }

    private void ensureOpen() {
        if (closed || webSocket == null) {
            throw new GameClientException("CLIENT_CLOSED", "GameClient is closed");
        }
    }

    private void failPending(GameClientException exception) {
        pending.values().forEach(future -> future.completeExceptionally(exception));
    }

    private void recordConnectionFailure(GameClientException exception) {
        connectionFailure = exception;
        failPending(exception);
        synchronized (stateMonitor) {
            stateMonitor.notifyAll();
        }
    }

    private static String withToken(String url, String token) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
