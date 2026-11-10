package com.antcolonywars.demo;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.GameClient;
import com.antcolonywars.client.GameState;

public final class WinningScenario {
    private WinningScenario() {
    }

    public static void main(String[] args) {
        try (GameClient bot = GameClient.connect(DemoConfig.serverUrl(), DemoConfig.blueToken())) {
            move(bot, Direction.RIGHT, 6);
            move(bot, Direction.DOWN, 6);
            destroy(bot, "hq-red");

            move(bot, Direction.UP, 3);
            move(bot, Direction.RIGHT, 16);
            destroy(bot, "hq-orange");

            move(bot, Direction.LEFT, 2);
            move(bot, Direction.DOWN, 4);
            move(bot, Direction.RIGHT, 2);
            destroy(bot, "hq-yellow");

            move(bot, Direction.LEFT, 2);
            move(bot, Direction.DOWN, 4);
            move(bot, Direction.RIGHT, 2);
            destroy(bot, "hq-magenta");

            move(bot, Direction.LEFT, 3);
            move(bot, Direction.DOWN, 4);
            GameState state = destroy(bot, "hq-maroon");
            System.out.printf(
                    "Match status: %s, winner: %s, revision: %d.%n",
                    state.status(),
                    state.winnerTeamId(),
                    state.revision()
            );
        }
    }

    private static void move(GameClient bot, Direction direction, int steps) {
        for (int step = 0; step < steps; step++) {
            bot.move(direction);
        }
    }

    private static GameState destroy(GameClient bot, String headquartersId) {
        for (int hit = 0; hit < 3; hit++) {
            bot.attack(headquartersId);
        }
        return bot.awaitSimulationIdle();
    }
}
