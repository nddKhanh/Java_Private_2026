package com.antcolonywars.desktop;

import com.antcolonywars.client.GameClient;
import com.antcolonywars.core.Unit;
import com.antcolonywars.server.GameServerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        classes = GameServerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:desktop-integration"
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class DesktopBotIntegrationTest {
    @LocalServerPort
    private int port;

    @Test
    void compiledSourceRunsInChildJvmAndMovesServerState() throws Exception {
        BotCompiler compiler = new BotCompiler();
        try (BotCompilation compilation = compiler.compile("MovingBot", BotCompilerTest.validSource("MovingBot"));
             BotProcessRunner runner = new BotProcessRunner()) {
            assertTrue(compilation.successful());

            BotRunResult result = runner.start(request(compilation, "blue-token", Duration.ofSeconds(10)))
                    .completion()
                    .get(15, TimeUnit.SECONDS);

            assertTrue(result.successful(), result.standardError());
            assertTrue(result.standardOutput().contains("moves-complete"));
            try (GameClient observer = GameClient.connect(serverUrl(), "blue-token")) {
                int x = observer.getLatestState().snapshot().units().stream()
                        .filter(unit -> unit.id().equals("worker-blue"))
                        .map(Unit::position)
                        .mapToInt(position -> position.x())
                        .findFirst()
                        .orElseThrow();
                assertEquals(4, x);
                assertEquals(3, observer.getLatestState().revision());
            }
        }
    }

    @Test
    void timeoutTerminatesInfiniteLoopWithoutStoppingDesktopProcess() throws Exception {
        BotCompiler compiler = new BotCompiler();
        try (BotCompilation compilation = compiler.compile("EndlessBot", endlessSource("EndlessBot"));
             BotProcessRunner runner = new BotProcessRunner()) {
            BotRunResult result = runner.start(request(compilation, "blue-token", Duration.ofSeconds(3)))
                    .completion()
                    .get(8, TimeUnit.SECONDS);

            assertTrue(result.timedOut());
            assertFalse(result.successful());
        }
    }

    @Test
    void stopTerminatesRunningChildJvm() throws Exception {
        BotCompiler compiler = new BotCompiler();
        try (BotCompilation compilation = compiler.compile("StoppableBot", endlessSource("StoppableBot"));
             BotProcessRunner runner = new BotProcessRunner()) {
            RunningBot process = runner.start(request(compilation, "blue-token", Duration.ofSeconds(30)));
            assertTrue(process.isAlive());

            process.stop();
            BotRunResult result = process.completion().get(5, TimeUnit.SECONDS);

            assertTrue(result.stopped());
            assertFalse(result.successful());
        }
    }

    @Test
    void invalidTokenFailsWithoutLeakingCredentialToOutput() throws Exception {
        BotCompiler compiler = new BotCompiler();
        String token = "private-invalid-token";
        try (BotCompilation compilation = compiler.compile("RejectedBot", BotCompilerTest.validSource("RejectedBot"));
             BotProcessRunner runner = new BotProcessRunner()) {
            BotRunResult result = runner.start(request(compilation, token, Duration.ofSeconds(5)))
                    .completion()
                    .get(10, TimeUnit.SECONDS);

            assertEquals(4, result.exitCode());
            assertFalse(result.standardOutput().contains(token));
            assertFalse(result.standardError().contains(token));
            assertTrue(result.standardError().contains("CONNECTION_FAILED"));
        }
    }

    private BotRunRequest request(BotCompilation compilation, String token, Duration timeout) {
        return new BotRunRequest(
                compilation.className(),
                compilation.classesDirectory(),
                serverUrl(),
                token,
                timeout,
                64,
                16 * 1024
        );
    }

    private String serverUrl() {
        return "ws://localhost:" + port + "/game";
    }

    private String endlessSource(String className) {
        return """
                import com.antcolonywars.bot.BotProgram;
                import com.antcolonywars.bot.GameBot;

                public final class %s implements BotProgram {
                    public void run(GameBot bot) {
                        while (true) {
                            Thread.onSpinWait();
                        }
                    }
                }
                """.formatted(className);
    }
}
