package com.antcolonywars.server;

import com.antcolonywars.core.ApplyResult;
import com.antcolonywars.core.MatchSetup;
import com.antcolonywars.core.MatchState;

import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MatchRegistry {
    private final Map<String, MatchCoordinator> coordinators;
    private final Map<String, TickMetrics> tickMetrics = new ConcurrentHashMap<>();

    MatchRegistry(
            MatchSetup template,
            List<String> matchIds,
            GameProfileProperties.Limits limits,
            Clock clock,
            MatchPersistence persistence
    ) {
        Map<String, MatchCoordinator> configured = new LinkedHashMap<>();
        for (String matchId : matchIds) {
            MatchSetup setup = copyForMatch(template, matchId);
            MatchState restored = persistence.load(matchId).orElse(null);
            MatchCoordinator coordinator = new MatchCoordinator(setup, restored, limits, clock, persistence);
            configured.put(matchId, coordinator);
            tickMetrics.put(matchId, new TickMetrics(0, 0, 0));
        }
        coordinators = java.util.Collections.unmodifiableMap(configured);
    }

    MatchRegistry(MatchCoordinator coordinator) {
        String matchId = coordinator.state().matchId();
        coordinators = Map.of(matchId, coordinator);
        tickMetrics.put(matchId, new TickMetrics(0, 0, 0));
    }

    public Optional<MatchCoordinator> find(String matchId) {
        return Optional.ofNullable(coordinators.get(matchId));
    }

    public MatchCoordinator require(String matchId) {
        MatchCoordinator coordinator = coordinators.get(matchId);
        if (coordinator == null) {
            throw new IllegalArgumentException("Unknown match: " + matchId);
        }
        return coordinator;
    }

    public List<ApplyResult> advanceTicks() {
        return coordinators.entrySet().stream().map(entry -> {
            long started = System.nanoTime();
            ApplyResult result = entry.getValue().advanceTick();
            long elapsedMicros = Math.max(0, (System.nanoTime() - started) / 1_000);
            tickMetrics.compute(entry.getKey(), (ignored, current) -> new TickMetrics(
                    elapsedMicros,
                    Math.max(current == null ? 0 : current.maxTickMicros(), elapsedMicros),
                    current == null ? 1 : current.sampleCount() + 1
            ));
            return result;
        }).toList();
    }

    public List<MatchRuntime> runtimes() {
        return coordinators.entrySet().stream().map(entry -> {
            MatchState state = entry.getValue().state();
            TickMetrics metrics = tickMetrics.get(entry.getKey());
            return new MatchRuntime(
                    state.matchId(), state.status().name(), state.revision(), state.simulationTick(),
                    metrics.lastTickMicros(), metrics.maxTickMicros(), metrics.sampleCount()
            );
        }).toList();
    }

    private MatchSetup copyForMatch(MatchSetup template, String matchId) {
        return new MatchSetup(
                template.setupId(), matchId, template.mapWidth(), template.mapHeight(), template.rules(),
                template.teams(), template.units(), template.headquarters(), template.resourceNodes(), template.buildings()
        );
    }

    public record MatchRuntime(
            String matchId,
            String status,
            long revision,
            long simulationTick,
            long lastTickMicros,
            long maxTickMicros,
            long tickSamples
    ) {
    }

    private record TickMetrics(long lastTickMicros, long maxTickMicros, long sampleCount) {
    }
}
