package com.antcolonywars.runner;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotRunnerMainTest {
    @Test
    void rejectsMissingConnectionConfiguration() {
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        int exitCode = BotRunnerMain.run(
                new String[]{"example.Bot"},
                Map.of(),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8),
                new PrintStream(errors, true, StandardCharsets.UTF_8)
        );

        assertEquals(2, exitCode);
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("GAME_WS_URL"));
    }

    @Test
    void rejectsClassThatDoesNotImplementBotProgram() {
        ByteArrayOutputStream errors = new ByteArrayOutputStream();

        int exitCode = BotRunnerMain.run(
                new String[]{String.class.getName()},
                Map.of(
                        BotRunnerMain.SERVER_URL_ENV, "ws://localhost:1/game",
                        BotRunnerMain.TEAM_TOKEN_ENV, "token"
                ),
                new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8),
                new PrintStream(errors, true, StandardCharsets.UTF_8)
        );

        assertEquals(3, exitCode);
        assertTrue(errors.toString(StandardCharsets.UTF_8).contains("Không thể nạp lớp"));
    }
}
