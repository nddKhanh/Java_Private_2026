package com.antcolonywars.server;

import com.antcolonywars.core.ActionCommand;
import com.antcolonywars.core.AttackCommand;
import com.antcolonywars.core.BuildCommand;
import com.antcolonywars.core.BuildingType;
import com.antcolonywars.core.CommandQueue;
import com.antcolonywars.core.CommandRejection;
import com.antcolonywars.core.Direction;
import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.HarvestCommand;
import com.antcolonywars.core.Headquarters;
import com.antcolonywars.core.MatchState;
import com.antcolonywars.core.MoveCommand;
import com.antcolonywars.core.QueueTickResult;
import com.antcolonywars.core.SpawnCommand;
import com.antcolonywars.core.Unit;
import com.antcolonywars.core.WaitCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket protocol: COMMAND and BATCH append to bot queues and are acknowledged at once;
 * queued commands run one per bot per tick and reach every viewer through STATE_UPDATED.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    public static final int SCHEMA_VERSION = 8;

    private final MatchRegistry matchRegistry;
    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Autowired
    public GameWebSocketHandler(MatchRegistry matchRegistry, ObjectMapper objectMapper) {
        this.matchRegistry = matchRegistry;
        this.objectMapper = objectMapper;
    }

    GameWebSocketHandler(MatchCoordinator coordinator, ObjectMapper objectMapper) {
        this(new MatchRegistry(coordinator), objectMapper);
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        sendState(session, coordinator(session).state(), List.of(), List.of());
    }

    @Override
    protected synchronized void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ClientMessage request;
        try {
            request = objectMapper.readValue(message.getPayload(), ClientMessage.class);
        } catch (JsonProcessingException exception) {
            sendFailure(session, null, "INVALID_JSON");
            return;
        }

        if (request.schemaVersion() != SCHEMA_VERSION) {
            sendFailure(session, request.commandId(), "UNSUPPORTED_SCHEMA_VERSION");
            return;
        }
        MatchCoordinator coordinator = coordinator(session);
        MatchState state = coordinator.state();
        if (!state.matchId().equals(request.matchId())) {
            sendFailure(session, request.commandId(), "MATCH_NOT_FOUND");
            return;
        }
        if ("GET_STATE".equals(request.type())) {
            send(session, CommandResultMessage.success(
                    state.matchId(), request.commandId(), state.revision(), state.eventSequence()
            ));
            sendState(session, state, List.of(), List.of());
            return;
        }
        String teamId = teamId(session);
        if (teamId == null) {
            sendFailure(session, request.commandId(), "UNAUTHORIZED");
            return;
        }
        switch (request.type() == null ? "" : request.type()) {
            case "COMMAND" -> handleEnqueue(session, teamId, request.commandId(), List.of(new BatchCommandMessage(
                    request.commandId(), request.action(), request.unitId(), request.displayName(),
                    request.unitType(), request.direction(), request.targetId(), request.buildingId(),
                    request.buildingType(), request.sourceId()
            )), coordinator);
            case "BATCH" -> handleEnqueue(session, teamId, request.commandId(), request.commands(), coordinator);
            case "CLEAR_QUEUE" -> handleClearQueue(session, teamId, request, coordinator);
            default -> sendFailure(session, request.commandId(), "INVALID_MESSAGE_TYPE");
        }
    }

    /**
     * Converts and queues a batch. A command that cannot be read is refused together with the later commands
     * of the same bot in this batch; other bots keep theirs.
     */
    private void handleEnqueue(
            WebSocketSession session,
            String teamId,
            String batchId,
            List<BatchCommandMessage> items,
            MatchCoordinator coordinator
    ) throws IOException {
        if (batchId == null || batchId.isBlank() || items == null || items.isEmpty()
                || items.size() > coordinator.maxBatchCommands()) {
            sendFailure(session, batchId, "INVALID_BATCH");
            return;
        }
        MatchState state = coordinator.state();
        List<ActionCommand> commands = new ArrayList<>();
        List<Integer> indexes = new ArrayList<>();
        List<RejectionMessage> rejections = new ArrayList<>();
        Map<String, RejectionMessage> blocked = new LinkedHashMap<>();
        Map<String, Integer> droppedBySubject = new LinkedHashMap<>();
        for (int index = 0; index < items.size(); index++) {
            BatchCommandMessage item = items.get(index);
            String subject = subjectOf(item, teamId, state);
            if (blocked.containsKey(subject)) {
                droppedBySubject.merge(subject, 1, Integer::sum);
                continue;
            }
            try {
                commands.add(toCommand(item, teamId, state));
                indexes.add(index);
            } catch (IllegalArgumentException | NullPointerException exception) {
                String commandId = item == null || item.commandId() == null ? batchId + ":" + index : item.commandId();
                String action = item == null ? null : item.action();
                blocked.put(subject, RejectionMessage.onArrival(
                        batchId, index, commandId, subject, action, "INVALID_COMMAND", 0
                ));
            }
        }
        for (Map.Entry<String, RejectionMessage> entry : blocked.entrySet()) {
            RejectionMessage first = entry.getValue();
            rejections.add(RejectionMessage.onArrival(
                    batchId, first.index(), first.commandId(), first.subjectId(), first.action(), first.error(),
                    droppedBySubject.getOrDefault(entry.getKey(), 0)
            ));
        }

        MatchCoordinator.EnqueueResult result = coordinator.enqueue(teamId, batchId, commands);
        for (String subject : result.refusedSubjects()) {
            List<ActionCommand> share = commands.stream()
                    .filter(command -> CommandQueue.subjectOf(command).equals(subject))
                    .toList();
            ActionCommand first = share.getFirst();
            rejections.add(RejectionMessage.onArrival(
                    batchId, indexes.get(commands.indexOf(first)), first.commandId(), subject,
                    CommandRejection.actionOf(first), result.matchFinished() ? "MATCH_FINISHED" : "COMMAND_QUEUE_FULL",
                    share.size() - 1
            ));
        }
        rejections.sort(java.util.Comparator.comparing(RejectionMessage::index));
        send(session, CommandResultMessage.queued(
                state.matchId(), batchId, result.queuedCount(), rejections, state.revision(), state.eventSequence()
        ));
        if (result.queuedCount() > 0) {
            sendTeamState(state, teamId);
        }
    }

    private void handleClearQueue(
            WebSocketSession session,
            String teamId,
            ClientMessage request,
            MatchCoordinator coordinator
    ) throws IOException {
        if (request.subjectId() == null || request.subjectId().isBlank()) {
            sendFailure(session, request.commandId(), "INVALID_COMMAND");
            return;
        }
        int removed = coordinator.clearQueue(teamId, request.subjectId());
        MatchState state = coordinator.state();
        send(session, CommandResultMessage.queued(
                state.matchId(), request.commandId(), removed, List.of(), state.revision(), state.eventSequence()
        ));
        if (removed > 0) {
            sendTeamState(state, teamId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        sessions.remove(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    public synchronized void broadcastSimulationUpdate(QueueTickResult result) {
        if (!result.changed()) {
            return;
        }
        // A tick that only failed commands concerns just the teams that sent them.
        Set<String> rejectedTeams = new java.util.HashSet<>();
        result.rejections().forEach(rejection -> rejectedTeams.add(rejection.teamId()));
        broadcast(
                result.state(),
                session -> !result.events().isEmpty() || rejectedTeams.contains(teamId(session)),
                result.events(),
                result.rejections()
        );
    }

    private ActionCommand toCommand(BatchCommandMessage request, String teamId, MatchState state) {
        requireText(request.commandId());
        requireText(request.action());
        if ("SPAWN".equals(request.action())) {
            return new SpawnCommand(
                    request.commandId(),
                    teamId,
                    spawnSource(request, teamId, state),
                    requireText(request.unitId()),
                    requireText(request.displayName()),
                    request.unitType() == null || request.unitType().isBlank() ? "WORKER" : request.unitType(),
                    Direction.valueOf(requireText(request.direction()))
            );
        }
        String unitId = request.unitId();
        if (unitId == null || unitId.isBlank()) {
            unitId = controlledUnitId(state, teamId);
        }
        return switch (request.action()) {
            case "MOVE" -> new MoveCommand(
                    request.commandId(), teamId, unitId, Direction.valueOf(requireText(request.direction()))
            );
            case "WAIT" -> new WaitCommand(request.commandId(), teamId, unitId);
            case "ATTACK" -> new AttackCommand(
                    request.commandId(), teamId, unitId, Direction.valueOf(requireText(request.direction()))
            );
            case "HARVEST" -> new HarvestCommand(request.commandId(), teamId, unitId);
            case "BUILD" -> new BuildCommand(
                    request.commandId(), teamId, unitId, requireText(request.buildingId()),
                    BuildingType.valueOf(requireText(request.buildingType())),
                    Direction.valueOf(requireText(request.direction()))
            );
            default -> throw new IllegalArgumentException("Unknown action");
        };
    }

    /** The bot whose queue receives a command, resolved the same way even when the command cannot be read. */
    private String subjectOf(BatchCommandMessage request, String teamId, MatchState state) {
        if (request == null) {
            return "";
        }
        if ("SPAWN".equals(request.action())) {
            return spawnSource(request, teamId, state);
        }
        if (request.unitId() != null && !request.unitId().isBlank()) {
            return request.unitId();
        }
        try {
            return controlledUnitId(state, teamId);
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    /** SPAWN without a source comes from the team's headquarters. */
    private String spawnSource(BatchCommandMessage request, String teamId, MatchState state) {
        if (request.sourceId() != null && !request.sourceId().isBlank()) {
            return request.sourceId();
        }
        return state.headquarters().stream()
                .filter(hq -> hq.teamId().equals(teamId) && hq.hp() > 0)
                .map(Headquarters::id)
                .findFirst()
                .orElse("");
    }

    private String controlledUnitId(MatchState state, String teamId) {
        return state.units().stream()
                .filter(unit -> unit.teamId().equals(teamId) && unit.hp() > 0)
                .map(Unit::id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Team has no active unit"));
    }

    private String requireText(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Required text is missing");
        }
        return value;
    }

    private void sendFailure(WebSocketSession session, String commandId, String error) throws IOException {
        MatchState state = coordinator(session).state();
        send(session, CommandResultMessage.failure(
                state.matchId(),
                commandId,
                error,
                state.revision(),
                state.eventSequence()
        ));
    }

    /** Queue changes without a state change only concern the team that owns the queue. */
    private void sendTeamState(MatchState state, String teamId) {
        broadcast(state, session -> teamId.equals(teamId(session)), List.of(), List.of());
    }

    private void broadcast(
            MatchState state,
            java.util.function.Predicate<WebSocketSession> audience,
            List<GameEvent> events,
            List<CommandRejection> rejections
    ) {
        sessions.removeIf(session -> {
            if (!session.isOpen()) {
                return true;
            }
            if (!state.matchId().equals(session.getAttributes().get(TokenHandshakeInterceptor.MATCH_ID_ATTRIBUTE))
                    || !audience.test(session)) {
                return false;
            }
            try {
                sendState(session, state, events, rejections);
                return false;
            } catch (IOException exception) {
                return true;
            }
        });
    }

    /**
     * Every connection sees the stock, queues and failed commands of its own team only;
     * other teams, allies included, lose wood and meat.
     */
    private void sendState(
            WebSocketSession session,
            MatchState state,
            List<GameEvent> events,
            List<CommandRejection> rejections
    ) throws IOException {
        String viewerTeamId = teamId(session);
        Map<String, List<QueuedCommandView>> queues = new LinkedHashMap<>();
        if (viewerTeamId != null) {
            coordinator(session).pendingCommands(viewerTeamId).forEach((subject, commands) ->
                    queues.put(subject, commands.stream().map(QueuedCommandView::of).toList()));
        }
        List<RejectionMessage> ownRejections = rejections.stream()
                .filter(rejection -> rejection.teamId().equals(viewerTeamId))
                .map(RejectionMessage::onTurn)
                .toList();
        ObjectNode payload = objectMapper.valueToTree(StateUpdatedMessage.from(viewerTeamId, state, events, queues, ownRejections));
        for (JsonNode team : payload.path("state").path("teams")) {
            if (team instanceof ObjectNode node && !node.path("id").asText().equals(viewerTeamId)) {
                node.remove(List.of("wood", "meat"));
            }
        }
        send(session, payload);
    }

    private String teamId(WebSocketSession session) {
        return (String) session.getAttributes().get(TokenHandshakeInterceptor.TEAM_ID_ATTRIBUTE);
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private MatchCoordinator coordinator(WebSocketSession session) {
        String matchId = (String) session.getAttributes().get(TokenHandshakeInterceptor.MATCH_ID_ATTRIBUTE);
        return matchRegistry.require(matchId);
    }
}
