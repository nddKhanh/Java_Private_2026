package com.antcolonywars.runner;

import com.antcolonywars.bot.BotProgram;
import com.antcolonywars.bot.GameBot;
import com.antcolonywars.bot.GameBots;
import com.antcolonywars.client.GameClientException;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Map;

public final class BotRunnerMain {
    static final String SERVER_URL_ENV = "GAME_WS_URL";
    static final String TEAM_TOKEN_ENV = "GAME_TEAM_TOKEN";
    static final String CONNECTION_TIMEOUT_ENV = "GAME_CONNECTION_TIMEOUT_SECONDS";

    private BotRunnerMain() {
    }

    public static void main(String[] args) {
        int exitCode = run(args, System.getenv(), System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args, Map<String, String> environment, PrintStream out, PrintStream err) {
        if (args.length != 1 || args[0].isBlank()) {
            err.println("Runner requires exactly one bot class name.");
            return 2;
        }

        String serverUrl = requiredEnvironment(environment, SERVER_URL_ENV, err);
        String teamToken = requiredEnvironment(environment, TEAM_TOKEN_ENV, err);
        if (serverUrl == null || teamToken == null) {
            return 2;
        }

        BotProgram program;
        try {
            Class<?> candidate = Class.forName(args[0]);
            if (!BotProgram.class.isAssignableFrom(candidate)) {
                err.printf("Class %s does not implement %s.%n", args[0], BotProgram.class.getName());
                return 3;
            }
            program = (BotProgram) candidate.getConstructor().newInstance();
        } catch (ReflectiveOperationException | LinkageError exception) {
            err.printf("Cannot load bot class %s: %s%n", args[0], messageOf(exception));
            return 3;
        }

        try (GameBot bot = GameBots.connect(serverUrl, teamToken, connectionTimeout(environment))) {
            out.printf("Connected. Running %s.%n", args[0]);
            program.run(bot);
            out.println("Bot finished successfully.");
            return 0;
        } catch (Throwable exception) {
            String code = exception instanceof GameClientException clientException
                    ? clientException.code()
                    : exception.getClass().getSimpleName();
            err.printf("Bot failed: %s: %s%n", code, messageOf(exception));
            return 4;
        }
    }

    private static String requiredEnvironment(Map<String, String> environment, String name, PrintStream err) {
        String value = environment.get(name);
        if (value == null || value.isBlank()) {
            err.printf("Missing required environment variable %s.%n", name);
            return null;
        }
        return value;
    }

    private static Duration connectionTimeout(Map<String, String> environment) {
        String value = environment.get(CONNECTION_TIMEOUT_ENV);
        if (value == null || value.isBlank()) {
            return Duration.ofSeconds(5);
        }
        try {
            return Duration.ofSeconds(Math.max(1, Long.parseLong(value)));
        } catch (NumberFormatException exception) {
            return Duration.ofSeconds(5);
        }
    }

    private static String messageOf(Throwable exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "no details" : message;
    }
}
