package com.antcolonywars.desktop;

final class DefaultBotSource {
    static final String CLASS_NAME = "MyBot";
    static final String SOURCE = """
            import com.antcolonywars.bot.BotProgram;
            import com.antcolonywars.bot.GameBot;
            import com.antcolonywars.client.Direction;

            public final class MyBot implements BotProgram {
                @Override
                public void run(GameBot bot) {
                    for (int step = 0; step < 3; step++) {
                        bot.move(Direction.RIGHT);
                    }

                    System.out.println("Completed 3 moves.");
                }
            }
            """;

    private DefaultBotSource() {
    }
}
