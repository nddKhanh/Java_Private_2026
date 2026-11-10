package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public final class SimulationTicker {
    private final MatchCoordinator coordinator;
    private final GameWebSocketHandler webSocketHandler;

    public SimulationTicker(MatchCoordinator coordinator, GameWebSocketHandler webSocketHandler) {
        this.coordinator = coordinator;
        this.webSocketHandler = webSocketHandler;
    }

    @Scheduled(fixedRateString = "${game.profile.limits.tick-millis:250}")
    public void tick() {
        ApplyResult result = coordinator.advanceTick();
        webSocketHandler.broadcastSimulationUpdate(result);
    }
}
