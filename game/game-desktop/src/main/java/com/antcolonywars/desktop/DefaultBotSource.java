package com.antcolonywars.desktop;

final class DefaultBotSource {
    static final String CLASS_NAME = "MyBot";
    static final String SOURCE = """
            import com.antcolonywars.bot.BotProgram;
            import com.antcolonywars.bot.GameBot;
            import com.antcolonywars.client.Direction;
            import com.antcolonywars.core.UnitType;

            public final class MyBot implements BotProgram {
                @Override
                public void run(GameBot bot) {
                    var state = bot.getLatestState().snapshot();
                    System.out.printf("Match %s: %d units, %d resources.%n",
                            state.matchId(), state.units().size(), state.resourceNodes().size());

                    // Spawn any supported type next to your headquarters:
                    // bot.spawn("ranged-1", "Ranged 1", UnitType.RANGED, Direction.DOWN);
                    // Attack from a selected unit in one direction up to its maximum range:
                    // bot.attack("ranged-1", Direction.RIGHT);

                    bot.waitTurn();
                    System.out.println("Bot command accepted.");
                }
            }
            """;

    private DefaultBotSource() {
    }
}
