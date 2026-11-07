package com.antcolonywars.server;

import com.antcolonywars.core.ActionCommand;
import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.AttackCommand;
import com.antcolonywars.core.Direction;
import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.GetStateCommand;
import com.antcolonywars.core.MatchState;
import com.antcolonywars.core.MoveCommand;
import com.antcolonywars.core.Unit;
import com.antcolonywars.core.WaitCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    public static final int SCHEMA_VERSION = 1;

    private final MatchCoordinator coordinator;
    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public GameWebSocketHandler(MatchCoordinator coordinator, ObjectMapper objectMapper) {
        this.coordinator = coordinator;
        this.objectMapper = objectMapper;
    }

    @Override
    public synchronized void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        sendState(session, coordinator.state(), null);
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
        MatchState state = coordinator.state();
        if (!state.matchId().equals(request.matchId())) {
            sendFailure(session, request.commandId(), "MATCH_NOT_FOUND");
            return;
        }
        if ("GET_STATE".equals(request.type())) {
            handleGetState(session, request);
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

    private void handleGetState(WebSocketSession session, ClientMessage request) throws IOException {
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
        requireText(request.commandId());
        requireText(request.action());
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
                    request.commandId(), teamId, unitId, requireText(request.targetId())
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
        MatchState state = coordinator.state();
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
            try {
                sendState(session, state, event);
                return false;
            } catch (IOException exception) {
                return true;
            }
        });
    }

    private void sendState(WebSocketSession session, MatchState state, GameEvent event) throws IOException {
        send(session, StateUpdatedMessage.from(state, event));
    }

    private void send(WebSocketSession session, Object payload) throws IOException {
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
    }
}
