package com.antcolonywars.server;

import com.antcolonywars.core.ActionCommand;
import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.BatchApplyResult;
import com.antcolonywars.core.BatchMode;
import com.antcolonywars.core.AttackCommand;
import com.antcolonywars.core.BuildCommand;
import com.antcolonywars.core.BuildingType;
import com.antcolonywars.core.Direction;
import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.GetStateCommand;
import com.antcolonywars.core.MatchState;
import com.antcolonywars.core.MoveCommand;
import com.antcolonywars.core.HarvestCommand;
import com.antcolonywars.core.SpawnCommand;
import com.antcolonywars.core.Unit;
import com.antcolonywars.core.WaitCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    public static final int SCHEMA_VERSION = 7;

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
        sendState(session, coordinator(session).state(), null);
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
            handleGetState(session, request, coordinator);
            return;
        }
        if ("BATCH".equals(request.type())) {
            handleBatch(session, request, coordinator, state);
            return;
        }
        if (!"COMMAND".equals(request.type())) {
            sendFailure(session, request.commandId(), "INVALID_MESSAGE_TYPE");
            return;
        }

        String teamId = (String) session.getAttributes().get(TokenHandshakeInterceptor.TEAM_ID_ATTRIBUTE);
        if (teamId == null) {
            sendFailure(session, request.commandId(), "UNAUTHORIZED");
            return;
        }

        ActionCommand command;
        try {
            command = toCommand(request, teamId, state);
        } catch (IllegalArgumentException exception) {
            sendFailure(session, request.commandId(), "INVALID_COMMAND");
            return;
        }

        ApplyResult result = coordinator.apply(command);
        if (!result.accepted()) {
            send(session, CommandResultMessage.failure(
                    result.state().matchId(),
                    request.commandId(),
                    result.error().name(),
                    result.state().revision(),
                    result.state().eventSequence()
            ));
            return;
        }

        send(session, CommandResultMessage.success(
                result.state().matchId(),
                request.commandId(),
                result.state().revision(),
                result.state().eventSequence()
        ));
        GameEvent event = result.events().isEmpty() ? null : result.events().getFirst();
        broadcastState(result.state(), event);
    }

    private void handleBatch(
            WebSocketSession session,
            ClientMessage request,
            MatchCoordinator coordinator,
            MatchState state
    ) throws IOException {
        String teamId = (String) session.getAttributes().get(TokenHandshakeInterceptor.TEAM_ID_ATTRIBUTE);
        if (teamId == null) {
            sendFailure(session, request.commandId(), "UNAUTHORIZED");
            return;
        }
        if (request.commands() == null || request.commands().isEmpty()) {
            sendFailure(session, request.commandId(), "INVALID_BATCH");
            return;
        }

        BatchMode mode;
        List<ActionCommand> commands = new ArrayList<>();
        try {
            mode = BatchMode.valueOf(requireText(request.mode()));
            for (BatchCommandMessage command : request.commands()) {
                commands.add(toCommand(command, teamId, state));
            }
        } catch (IllegalArgumentException exception) {
            sendFailure(session, request.commandId(), "INVALID_COMMAND");
            return;
        }

        BatchApplyResult result = coordinator.applyBatch(commands, mode);
        send(session, CommandResultMessage.batch(
                result.state().matchId(),
                request.commandId(),
                result.error() == null ? null : result.error().name(),
                result.failedIndex() < 0 ? null : result.failedIndex(),
                result.appliedCount(),
                result.state().revision(),
                result.state().eventSequence()
        ));
        if (!result.events().isEmpty()) {
            GameEvent event = result.events().getLast();
            broadcastState(result.state(), event);
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

    private void handleGetState(
            WebSocketSession session,
            ClientMessage request,
            MatchCoordinator coordinator
    ) throws IOException {
        MatchState state = coordinator.state();
        send(session, CommandResultMessage.success(
                state.matchId(),
                request.commandId(),
                state.revision(),
                state.eventSequence()
        ));
        sendState(session, state, null);
    }

    private ActionCommand toCommand(ClientMessage request, String teamId, MatchState state) {
        return toCommand(new BatchCommandMessage(
                request.commandId(), request.action(), request.unitId(), request.displayName(),
                request.unitType(), request.direction(), request.targetId(), request.buildingId(),
                request.buildingType(), request.sourceId()
        ), teamId, state);
    }

    private ActionCommand toCommand(BatchCommandMessage request, String teamId, MatchState state) {
        requireText(request.commandId());
        requireText(request.action());
        if ("SPAWN".equals(request.action())) {
            return new SpawnCommand(
                    request.commandId(),
                    teamId,
                    request.sourceId(),
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

    private void broadcastState(MatchState state, GameEvent event) {
        sessions.removeIf(session -> {
            if (!session.isOpen()) {
                return true;
            }
            if (!state.matchId().equals(session.getAttributes().get(TokenHandshakeInterceptor.MATCH_ID_ATTRIBUTE))) {
                return false;
            }
            try {
                sendState(session, state, event);
                return false;
            } catch (IOException exception) {
                return true;
            }
        });
    }

    public synchronized void broadcastSimulationUpdate(ApplyResult result) {
        if (!result.accepted() || result.events().isEmpty()) {
            return;
        }
        broadcastState(result.state(), result.events().getFirst());
    }

    private void sendState(WebSocketSession session, MatchState state, GameEvent event) throws IOException {
        send(session, StateUpdatedMessage.from(state, event));
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }

    private MatchCoordinator coordinator(WebSocketSession session) {
        String matchId = (String) session.getAttributes().get(TokenHandshakeInterceptor.MATCH_ID_ATTRIBUTE);
        return matchRegistry.require(matchId);
    }
}
