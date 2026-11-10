package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public final class SimulationTicker {
    private final MatchRegistry matchRegistry;
    private final GameWebSocketHandler webSocketHandler;

    @Autowired
    public SimulationTicker(MatchRegistry matchRegistry, GameWebSocketHandler webSocketHandler) {
        this.matchRegistry = matchRegistry;
        this.webSocketHandler = webSocketHandler;
    }

    SimulationTicker(MatchCoordinator coordinator, GameWebSocketHandler webSocketHandler) {
        this(new MatchRegistry(coordinator), webSocketHandler);
    }

    @Scheduled(fixedRateString = "${game.profile.limits.tick-millis:250}")
    public void tick() {
        for (ApplyResult result : matchRegistry.advanceTicks()) {
            webSocketHandler.broadcastSimulationUpdate(result);
        }
    }
}
