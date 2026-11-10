package com.antcolonywars.server;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GameProfilePropertiesTest {
    @Test
    void resolvesChangedTokensFromProfileWithoutHandlerChanges() {
        GameProfileProperties profile = new GameProfileProperties(
                "custom",
                "custom-match",
                new GameProfileProperties.Board(4, 4),
                new GameProfileProperties.Rules("custom-1", 1, 1, 5, 3, 0, 3, 0, 3, 2, 1, 5),
                List.of(new GameProfileProperties.TeamProfile("GREEN", "Green", "#00ff00", "custom-token")),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new GameProfileProperties.Limits(10, 32, 250)
        );

        assertEquals("GREEN", profile.teamIdForToken("custom-token"));
        assertNull(profile.teamIdForToken("blue-token"));
    }
}
