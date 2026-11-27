package com.antcolonywars.demo;

import com.antcolonywars.bot.GameBot;
import com.antcolonywars.bot.GameBots;
import com.antcolonywars.client.Direction;
import com.antcolonywars.core.UnitType;

public final class ReinforcementBot {
    private ReinforcementBot() {
    }

    public static void main(String[] args) {
        try (GameBot bot = GameBots.connect(DemoConfig.serverUrl(), DemoConfig.blueToken())) {
            bot.selectUnit("worker-blue");
            bot.move(Direction.RIGHT);
            bot.move(Direction.DOWN);
            bot.move(Direction.DOWN);
            bot.harvest();
            bot.spawn("hq-blue", "worker-blue-2", "Blue Worker 2", UnitType.WORKER, Direction.DOWN);
            bot.selectUnit("worker-blue-2");
            for (int step = 0; step < 3; step++) {
                bot.move(Direction.RIGHT);
            }
            System.out.printf("%s completed reinforcement demo at revision %d.%n",
                    bot.selectedUnitId(), bot.getLatestState().revision());
        }
    }
}
