package com.antcolonywars.server;

import com.antcolonywars.core.GameEvent;
import com.antcolonywars.core.MatchState;

import java.util.List;
import java.util.Optional;

public interface MatchPersistence {
    Optional<MatchState> load(String matchId);

    void save(MatchState state, GameEvent event);

    List<GameEvent> history(String matchId);

    static MatchPersistence none() {
        return NoOpMatchPersistence.INSTANCE;
    }

    enum NoOpMatchPersistence implements MatchPersistence {
        INSTANCE;

        @Override
        public Optional<MatchState> load(String matchId) {
            return Optional.empty();
        }

        @Override
        public void save(MatchState state, GameEvent event) {
        }

        @Override
        public List<GameEvent> history(String matchId) {
            return List.of();
        }
    }
}
