package com.antcolonywars.runner;

import com.antcolonywars.bot.BotProgram;
import com.antcolonywars.bot.GameBot;
import com.antcolonywars.bot.GameBots;
import com.antcolonywars.client.GameClientException;
import com.antcolonywars.core.BatchMode;
import com.antcolonywars.sdk.Bot;
import com.antcolonywars.sdk.BotAdapter;

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.util.Map;

public final class BotRunnerMain {
    static final String SERVER_URL_ENV = "GAME_WS_URL";
    static final String TEAM_TOKEN_ENV = "GAME_TEAM_TOKEN";
    static final String MATCH_ID_ENV = "GAME_MATCH_ID";
    static final String CONNECTION_TIMEOUT_ENV = "GAME_CONNECTION_TIMEOUT_SECONDS";
    static final String BATCH_MODE_ENV = "GAME_BATCH_MODE";

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
        String matchId = environment.getOrDefault(MATCH_ID_ENV, "").trim();
        if (serverUrl == null || teamToken == null) {
            return 2;
        }

        ProgramEntry program;
        try {
            Class<?> candidate = Class.forName(args[0]);
            if (BotProgram.class.isAssignableFrom(candidate)) {
                BotProgram legacy = (BotProgram) candidate.getConstructor().newInstance();
                program = legacy::run;
            } else {
                Method main = candidate.getMethod("main", Bot.class);
                if (!Modifier.isStatic(main.getModifiers()) || main.getReturnType() != void.class) {
                    throw new NoSuchMethodException("main(Bot) must be public static void");
                }
                program = gameBot -> invokeStudentMain(main, new BotAdapter(gameBot));
            }
        } catch (ReflectiveOperationException | LinkageError exception) {
            err.printf("Không thể nạp lớp %s: %s%n", args[0], messageOf(exception));
            return 3;
        }

        Duration timeout = connectionTimeout(environment);
        BatchMode batchMode = batchMode(environment);
        try (GameBot bot = GameBots.connectBatch(serverUrl, teamToken, matchId, timeout, batchMode)) {
            out.printf("Đã kết nối. Đang chạy %s.%n", args[0]);
            program.run(bot);
            out.println("Bot đã chạy xong.");
            return 0;
        } catch (Throwable exception) {
            String code = exception instanceof GameClientException clientException
                    ? clientException.code()
                    : exception.getClass().getSimpleName();
            err.printf("Lỗi bot [%s]: %s%n", code, messageOf(exception));
            return 4;
        }
    }

    private static void invokeStudentMain(Method method, Bot bot) throws Throwable {
        try {
            method.invoke(null, bot);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }

    @FunctionalInterface
    private interface ProgramEntry {
        void run(GameBot bot) throws Throwable;
    }

    private static BatchMode batchMode(Map<String, String> environment) {
        String value = environment.getOrDefault(BATCH_MODE_ENV, BatchMode.ATOMIC.name());
        try {
            return BatchMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            return BatchMode.ATOMIC;
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
