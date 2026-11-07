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
            move(bot, Direction.RIGHT, 6);

            GameState state = bot.getLatestState();
            if (!"FINISHED".equals(state.status())) {
                for (int hit = 0; hit < 3; hit++) {
                    state = bot.attack("hq-red");
                }
            }
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
}
