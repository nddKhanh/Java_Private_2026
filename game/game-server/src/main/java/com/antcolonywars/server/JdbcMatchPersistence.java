package com.antcolonywars.server;

import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.MatchState;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcMatchPersistence implements MatchPersistence {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public JdbcMatchPersistence(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public Optional<MatchState> load(String matchId) {
        return jdbcTemplate.query(
                "SELECT state_json FROM match_snapshot WHERE match_id = ?",
                (resultSet, rowNumber) -> read(resultSet.getString("state_json"), MatchState.class),
                matchId
        ).stream().findFirst();
    }

    @Override
    @Transactional
    public void save(MatchState state, GameEvent event) {
        String stateJson = write(state);
        int updated = jdbcTemplate.update(
                "UPDATE match_snapshot SET state_json = ?, updated_at_ms = ? WHERE match_id = ?",
                stateJson, clock.millis(), state.matchId()
        );
        if (updated == 0) {
            jdbcTemplate.update(
                    "INSERT INTO match_snapshot(match_id, state_json, updated_at_ms) VALUES (?, ?, ?)",
                    state.matchId(), stateJson, clock.millis()
            );
        }
        if (event != null) {
            jdbcTemplate.update(
                    "INSERT INTO match_event(match_id, event_sequence, event_json, created_at_ms) VALUES (?, ?, ?, ?)",
                    state.matchId(), event.sequence(), write(event), clock.millis()
            );
        }
    }

    @Override
    public List<GameEvent> history(String matchId) {
        return jdbcTemplate.query(
                "SELECT event_json FROM match_event WHERE match_id = ? ORDER BY event_sequence",
                (resultSet, rowNumber) -> read(resultSet.getString("event_json"), GameEvent.class),
                matchId
        );
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot serialize persisted match data", exception);
        }
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Cannot deserialize persisted match data", exception);
        }
    }
}
