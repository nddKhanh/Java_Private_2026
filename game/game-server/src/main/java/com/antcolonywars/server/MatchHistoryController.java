package com.antcolonywars.server;

import com.antcolonywars.core.GameEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
public final class MatchHistoryController {
    private final MatchPersistence persistence;
    private final MatchRegistry matchRegistry;

    public MatchHistoryController(MatchPersistence persistence, MatchRegistry matchRegistry) {
        this.persistence = persistence;
        this.matchRegistry = matchRegistry;
    }

    @GetMapping
    public List<MatchRegistry.MatchRuntime> matches() {
        return matchRegistry.runtimes();
    }

    @GetMapping("/{matchId}/history")
    public List<GameEvent> history(@PathVariable("matchId") String matchId) {
        return persistence.history(matchId);
    }
}
