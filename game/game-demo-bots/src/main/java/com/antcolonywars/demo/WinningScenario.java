package com.antcolonywars.demo;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.GameClient;
import com.antcolonywars.client.GameState;

public final class WinningScenario {
    private WinningScenario() {
    }

    public static void main(String[] args) {
        try (GameClient bot = GameClient.connect(DemoConfig.serverUrl(), DemoConfig.blueToken())) {
            move(bot, Direction.DOWN, 6);
            move(bot, Direction.RIGHT, 48);
            move(bot, Direction.UP, 5);
            destroy(bot, Direction.UP);

            move(bot, Direction.DOWN, 10);
            destroy(bot, Direction.DOWN);

            move(bot, Direction.DOWN, 12);
            destroy(bot, Direction.DOWN);

            move(bot, Direction.DOWN, 12);
            destroy(bot, Direction.DOWN);

            move(bot, Direction.DOWN, 13);
            GameState state = destroy(bot, Direction.DOWN);
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

    private static GameState destroy(GameClient bot, Direction direction) {
        for (int hit = 0; hit < 3; hit++) {
            bot.attack(direction);
        }
        return bot.awaitSimulationIdle();
    }
}
