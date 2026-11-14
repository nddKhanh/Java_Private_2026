package com.antcolonywars.desktop;

final class DefaultBotSource {
    static final String CLASS_NAME = "Main";
    static final String SOURCE = """
            import com.antcolonywars.sdk.Bot;

            public final class Main {
                public static void main(Bot bot) {
                    var state = bot.state().snapshot();
                    System.out.printf("Match %s: %d units, %d resources.%n",
                            state.matchId(), state.units().size(), state.resourceNodes().size());

                    bot.select("worker-blue");
                    bot.go(2);
                    bot.idle();
                }
            }
            """;

    private DefaultBotSource() {
    }
}
