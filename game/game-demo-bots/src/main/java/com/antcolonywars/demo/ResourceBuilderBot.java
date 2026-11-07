package com.antcolonywars.demo;

import com.antcolonywars.bot.GameBot;
import com.antcolonywars.bot.GameBots;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.client.Direction;

public final class ResourceBuilderBot {
    private ResourceBuilderBot() {
    }

    public static void main(String[] args) {
        try (GameBot bot = GameBots.connect(DemoConfig.serverUrl(), DemoConfig.blueToken())) {
            bot.selectUnit("worker-blue");
            bot.move(Direction.RIGHT);
            bot.move(Direction.RIGHT);
            bot.move(Direction.DOWN);
            bot.harvest();
            bot.build("barracks-blue", BuildingType.BARRACKS, Direction.RIGHT);
            System.out.printf("Built a barracks at revision %d.%n", bot.getLatestState().revision());
        }
    }
}
