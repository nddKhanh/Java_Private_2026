package com.antcolonywars.demo;

import com.antcolonywars.bot.GameBot;
import com.antcolonywars.client.Direction;
import com.antcolonywars.client.GameState;
import com.antcolonywars.core.Position;

final class DemoConfig {
    private DemoConfig() {
    }

    static String serverUrl() {
        return environmentOrDefault("GAME_WS_URL", "ws://localhost:8080/game");
    }

    static String blueToken() {
        return environmentOrDefault("GAME_BLUE_TOKEN", "blue-token");
    }

    static String redToken() {
        return environmentOrDefault("GAME_RED_TOKEN", "red-token");
    }

    /** The starter worker spawns on a random cell next to its headquarters, so demos read where it stands. */
    static Position position(GameState state, String unitId) {
        return state.snapshot().units().stream()
                .filter(unit -> unit.id().equals(unitId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unit not found: " + unitId))
                .position();
    }

    /** Walks the selected unit right or left first, then down or up. */
    static void walkTo(GameBot bot, String unitId, int x, int y) {
        Position start = position(bot.getLatestState(), unitId);
        for (int step = 0; step < Math.abs(x - start.x()); step++) {
            bot.move(x > start.x() ? Direction.RIGHT : Direction.LEFT);
        }
        for (int step = 0; step < Math.abs(y - start.y()); step++) {
            bot.move(y > start.y() ? Direction.DOWN : Direction.UP);
        }
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
