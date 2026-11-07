package com.antcolonywars.desktop;

final class DefaultBotSource {
    static final String CLASS_NAME = "MyBot";
    static final String SOURCE = """
            import com.antcolonywars.bot.BotProgram;
            import com.antcolonywars.bot.GameBot;
            import com.antcolonywars.client.Direction;
            import com.antcolonywars.client.BuildingType;

            public final class MyBot implements BotProgram {
                @Override
                public void run(GameBot bot) {
                    bot.spawn("worker-blue-2", "Blue Worker 2", Direction.DOWN);
                    bot.selectUnit("worker-blue-2");
                    for (int step = 0; step < 2; step++) {
                        bot.move(Direction.RIGHT);
                    }
                    bot.move(Direction.DOWN);
                    bot.harvest();
                    bot.build("barracks-blue", BuildingType.BARRACKS, Direction.RIGHT);

                    System.out.println("Spawned, harvested and built a barracks.");
                }
            }
            """;

    private DefaultBotSource() {
    }
}
