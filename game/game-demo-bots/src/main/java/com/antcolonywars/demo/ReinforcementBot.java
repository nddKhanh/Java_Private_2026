package com.antcolonywars.demo;

import com.antcolonywars.bot.GameBot;
import com.antcolonywars.bot.GameBots;
import com.antcolonywars.client.Direction;

public final class ReinforcementBot {
    private ReinforcementBot() {
    }

    public static void main(String[] args) {
        try (GameBot bot = GameBots.connect(DemoConfig.serverUrl(), DemoConfig.blueToken())) {
            bot.spawn("worker-blue-2", "Blue Worker 2", Direction.DOWN);
            bot.selectUnit("worker-blue-2");
            for (int step = 0; step < 3; step++) {
                bot.move(Direction.RIGHT);
            }
            System.out.printf("%s completed reinforcement demo at revision %d.%n",
                    bot.selectedUnitId(), bot.getLatestState().revision());
        }
    }
}
