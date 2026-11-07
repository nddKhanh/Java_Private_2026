package com.antcolonywars.client;

import com.antcolonywars.core.MatchState;

import java.util.Objects;

public record GameState(MatchState snapshot) {
    public GameState {
        Objects.requireNonNull(snapshot, "snapshot");
    }

    public long revision() {
        return snapshot.revision();
    }

    public long sequence() {
        return snapshot.eventSequence();
    }

    public String status() {
        return snapshot.status().name();
    }

    public String winnerTeamId() {
        return snapshot.winnerTeamId();
    }
}
