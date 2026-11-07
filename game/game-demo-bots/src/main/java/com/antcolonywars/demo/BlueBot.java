package com.antcolonywars.demo;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.GameClient;
import com.antcolonywars.client.GameState;

public final class BlueBot {
    private BlueBot() {
    }

    public static void main(String[] args) {
        try (GameClient bot = GameClient.connect(DemoConfig.serverUrl(), DemoConfig.blueToken())) {
            for (int step = 0; step < 3; step++) {
                bot.move(Direction.RIGHT);
            }
            GameState state = bot.getLatestState();
            System.out.printf("BLUE completed 3 moves at revision %d.%n", state.revision());
        }
    }
}
