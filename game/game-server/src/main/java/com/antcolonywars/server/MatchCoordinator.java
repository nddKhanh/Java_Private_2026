package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.GameCommand;
import com.antcolonywars.core.GameSession;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.MatchState;

public final class MatchCoordinator {
    private final GameSession session;

    public MatchCoordinator(MatchSetup setup) {
        session = new GameSession(setup);
    }

    public synchronized ApplyResult apply(GameCommand command) {
        return session.apply(command);
    }

    public synchronized MatchState state() {
        return session.state();
    }
}
