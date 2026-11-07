package com.antcolonywars.bot;

@FunctionalInterface
public interface BotProgram {
    void run(GameBot bot) throws Exception;
}
