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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class GameClient implements AutoCloseable, WebSocket.Listener {
    private static final int SCHEMA_VERSION = 8;
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    /** A single command waits behind its bot's queue, one command per tick, so its result can take a while. */
    private static final Duration EXECUTION_TIMEOUT = Duration.ofSeconds(60);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Duration timeout;
    private final Map<String, CompletableFuture<CommandResult>> pending = new ConcurrentHashMap<>();
    private final Map<String, CompletableFuture<GameState>> executions = new ConcurrentHashMap<>();
    private final Object stateMonitor = new Object();
    private final StringBuilder textFragments = new StringBuilder();
    private volatile MatchState latestState;
    private volatile Throwable connectionFailure;
    private volatile boolean closed;
    private WebSocket webSocket;

    private GameClient(Duration timeout) {
        this.timeout = timeout;
    }

    public static GameClient connect(String url, String accessToken) {
        return connect(url, accessToken, null, DEFAULT_TIMEOUT);
    }

    public static GameClient connect(String url, String accessToken, Duration timeout) {
        return connect(url, accessToken, null, timeout);
    }

    public static GameClient connect(String url, String accessToken, String matchId) {
        return connect(url, accessToken, matchId, DEFAULT_TIMEOUT);
    }

    public static GameClient connect(String url, String accessToken, String matchId, Duration timeout) {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(accessToken, "accessToken");
        Objects.requireNonNull(timeout, "timeout");
        GameClient client = new GameClient(timeout);
        client.open(withAccess(url, accessToken, matchId));
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

    public GameState attack(Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return sendAction("ATTACK", Map.of("direction", direction.name()));
    }

    public GameState attack(String unitId, Direction direction) {
        Objects.requireNonNull(direction, "direction");
        return sendUnitAction("ATTACK", unitId, Map.of("direction", direction.name()));
    }

    @Deprecated(forRemoval = true)
    public GameState spawn(String unitId, String displayName, Direction direction) {
        throw missingProductionSource();
    }

    @Deprecated(forRemoval = true)
    public GameState spawn(String unitId, String displayName, UnitType unitType, Direction direction) {
        throw missingProductionSource();
    }

    public GameState spawn(
            String sourceId,
            String unitId,
            String displayName,
            UnitType unitType,
            Direction direction
    ) {
        Objects.requireNonNull(unitId, "unitId");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(unitType, "unitType");
        Objects.requireNonNull(direction, "direction");
        if (sourceId == null || sourceId.isBlank()) {
            throw missingProductionSource();
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("unitId", unitId);
        payload.put("displayName", displayName);
        payload.put("unitType", unitType.name());
        payload.put("direction", direction.name());
        payload.put("sourceId", sourceId);
        return sendAction("SPAWN", payload);
    }

    private static GameClientException missingProductionSource() {
        return new GameClientException(
                "PRODUCTION_SOURCE_REQUIRED",
                "Thiếu ID công trình sản xuất. Hãy truyền sourceId của HQ hoặc BARRACKS."
        );
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

    /** Appends the actions to the queues of their bots and returns once the server has queued them. */
    public BatchReceipt submitBatch(List<BatchAction> actions) {
        Objects.requireNonNull(actions, "actions");
        String batchId = UUID.randomUUID().toString();
        if (actions.isEmpty()) {
            return new BatchReceipt(batchId, actions, 0, List.of());
        }
        Map<String, Object> message = new HashMap<>();
        message.put("type", "BATCH");
        message.put("commands", List.copyOf(actions));
        CommandResult result = request(batchId, message);
        if (!result.ok() && result.rejections().isEmpty()) {
            throw new GameClientException(result.error(), textOrDefault(result.errorMessage(), "Không thể gửi lệnh."));
        }
        return new BatchReceipt(batchId, actions, result.queuedCount(), result.rejections());
    }

    /** Removes every pending command of one of the team's bots; returns how many were removed. */
    public int clearQueue(String subjectId) {
        Objects.requireNonNull(subjectId, "subjectId");
        Map<String, Object> message = new HashMap<>();
        message.put("type", "CLEAR_QUEUE");
        message.put("subjectId", subjectId);
        CommandResult result = request(UUID.randomUUID().toString(), message);
        if (!result.ok()) {
            throw new GameClientException(result.error(), textOrDefault(result.errorMessage(), "Không thể xóa hàng chờ."));
        }
        return result.queuedCount();
    }

    /** Describes a refused command the way the console shows it: position, bot, action and reason. */
    static String describe(QueueRejection rejection, List<BatchAction> actions) {
        String prefix = "";
        if (rejection.index() != null && rejection.index() >= 0 && rejection.index() < actions.size()) {
            BatchAction action = actions.get(rejection.index());
            prefix = "Lệnh " + (rejection.index() + 1) + " — " + subjectId(action) + " — "
                    + actionDescription(action) + ": ";
        } else if (rejection.subjectId() != null && !rejection.subjectId().isBlank()) {
            prefix = rejection.subjectId() + " — " + textOrDefault(rejection.action(), "lệnh").toLowerCase() + ": ";
        }
        String detail = textOrDefault(rejection.errorMessage(), "Lệnh không thể thực hiện.");
        String dropped = rejection.droppedCount() > 0
                ? " Đã bỏ " + rejection.droppedCount() + " lệnh sau của bot này trong lần gửi."
                : "";
        return prefix + detail + dropped;
    }

    private static String subjectId(BatchAction action) {
        String id = "SPAWN".equals(action.action()) ? action.sourceId() : action.unitId();
        return id == null || id.isBlank() ? "mặc định" : id;
    }

    private static String actionDescription(BatchAction action) {
        String direction = directionLabel(action.direction());
        return switch (action.action()) {
            case "MOVE" -> "di chuyển" + direction;
            case "WAIT" -> "đứng yên";
            case "ATTACK" -> "tấn công" + direction;
            case "HARVEST" -> "khai thác";
            case "BUILD" -> "xây " + textOrDefault(action.buildingType(), "công trình") + direction;
            case "SPAWN" -> "spawn " + textOrDefault(action.unitType(), "quân") + direction;
            default -> textOrDefault(action.action(), "hành động").toLowerCase();
        };
    }

    private static String directionLabel(String direction) {
        if (direction == null || direction.isBlank()) {
            return "";
        }
        return " " + switch (direction) {
            case "UP" -> "lên";
            case "RIGHT" -> "phải";
            case "DOWN" -> "xuống";
            case "LEFT" -> "trái";
            default -> direction.toLowerCase();
        };
    }

    private static String textOrDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
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
        GameClientException failure = new GameClientException("CONNECTION_CLOSED", "WebSocket closed: " + reason);
        failPending(failure);
        recordConnectionFailure(failure);
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

    /** Queues one command and waits until it runs on its bot's turn, returning the state right after it. */
    private GameState sendAction(String action, Map<String, Object> parameters) {
        String commandId = UUID.randomUUID().toString();
        Map<String, Object> message = new HashMap<>(parameters);
        message.put("type", "COMMAND");
        message.put("action", action);
        CompletableFuture<GameState> execution = new CompletableFuture<>();
        executions.put(commandId, execution);
        try {
            CommandResult result = request(commandId, message);
            if (!result.ok()) {
                throw new GameClientException(
                        result.error(), textOrDefault(result.errorMessage(), "Lệnh không thể thực hiện.")
                );
            }
            return execution.get(EXECUTION_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        } catch (GameClientException exception) {
            throw exception;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GameClientException("INTERRUPTED", "Interrupted while waiting for command", exception);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof GameClientException clientException) {
                throw clientException;
            }
            throw new GameClientException("COMMAND_FAILED", "Cannot run command", exception.getCause());
        } catch (TimeoutException exception) {
            throw new GameClientException("TIMEOUT", "Timed out waiting for command " + commandId, exception);
        } finally {
            executions.remove(commandId);
        }
    }

    /** Sends one message and waits for its COMMAND_RESULT acknowledgement. */
    private CommandResult request(String commandId, Map<String, Object> message) {
        ensureOpen();
        message.put("schemaVersion", SCHEMA_VERSION);
        message.put("matchId", latestState.matchId());
        message.put("commandId", commandId);
        CompletableFuture<CommandResult> response = new CompletableFuture<>();
        pending.put(commandId, response);
        try {
            webSocket.sendText(objectMapper.writeValueAsString(message), true)
                    .get(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return response.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new GameClientException("INTERRUPTED", "Đã dừng khi đang gửi lệnh.", exception);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof GameClientException clientException) {
                throw clientException;
            }
            throw new GameClientException("COMMAND_FAILED", "Không thể gửi lệnh.", exception.getCause());
        } catch (TimeoutException exception) {
            throw new GameClientException("TIMEOUT", "Hết thời gian chờ server xác nhận lệnh.", exception);
        } catch (java.io.IOException exception) {
            throw new GameClientException("INVALID_MESSAGE", "Không thể mã hóa lệnh.", exception);
        } finally {
            pending.remove(commandId);
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
                for (JsonNode event : message.path("events")) {
                    CompletableFuture<GameState> execution = executions.get(event.path("commandId").asText());
                    if (execution != null) {
                        execution.complete(new GameState(state));
                    }
                }
                for (QueueRejection rejection : rejections(message.path("rejections"))) {
                    CompletableFuture<GameState> execution = executions.get(rejection.commandId());
                    if (execution != null) {
                        execution.completeExceptionally(new GameClientException(
                                rejection.error(), textOrDefault(rejection.errorMessage(), "Lệnh không thể thực hiện.")
                        ));
                    }
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
                            text(message.path("error")),
                            text(message.path("errorMessage")),
                            message.path("queuedCount").asInt(),
                            rejections(message.path("rejections")),
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
        executions.values().forEach(future -> future.completeExceptionally(exception));
    }

    private static List<QueueRejection> rejections(JsonNode node) {
        List<QueueRejection> result = new ArrayList<>();
        for (JsonNode item : node) {
            result.add(new QueueRejection(
                    text(item.path("commandId")),
                    text(item.path("subjectId")),
                    text(item.path("action")),
                    text(item.path("error")),
                    text(item.path("errorMessage")),
                    item.path("droppedCount").asInt(),
                    item.path("index").isNumber() ? item.path("index").asInt() : null
            ));
        }
        return result;
    }

    private static String text(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private void recordConnectionFailure(GameClientException exception) {
        connectionFailure = exception;
        failPending(exception);
        synchronized (stateMonitor) {
            stateMonitor.notifyAll();
        }
    }

    private static String withAccess(String url, String token, String matchId) {
        String separator = url.contains("?") ? "&" : "?";
        String authenticated = url + separator + "token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        if (matchId == null || matchId.isBlank()) {
            return authenticated;
        }
        return authenticated + "&matchId=" + URLEncoder.encode(matchId, StandardCharsets.UTF_8);
    }
}
