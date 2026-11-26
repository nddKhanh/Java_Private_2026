package com.antcolonywars.desktop;

import com.antcolonywars.core.BatchMode;

import com.antcolonywars.client.GameClient;
import com.antcolonywars.core.Unit;
import com.antcolonywars.server.GameServerApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
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
    void studentMainUsesShortApiAndSubmitsOneBatch() throws Exception {
        String source = """
                import com.antcolonywars.sdk.Bot;

                public final class Main {
                    public static void main(Bot bot) {
                        bot.select("hq-blue");
                        bot.spawn("worker-blue-2", "WORKER", 3);
                        bot.select("worker-blue-2");
                        bot.stay();
                    }
                }
                """;
        BotCompiler compiler = new BotCompiler();
        try (BotCompilation compilation = compiler.compile("Main", source);
             BotProcessRunner runner = new BotProcessRunner()) {
            BotRunResult result = runner.start(request(compilation, "blue-token", Duration.ofSeconds(10)))
                    .completion()
                    .get(15, TimeUnit.SECONDS);

            assertTrue(result.successful(), result.standardError());
            try (GameClient observer = GameClient.connect(serverUrl(), "blue-token")) {
                assertEquals(2, observer.getLatestState().revision());
                assertEquals(0, observer.getLatestState().snapshot().units().stream()
                        .filter(unit -> unit.id().equals("worker-blue-2"))
                        .findFirst().orElseThrow().position().x());
            }
        }
    }

    @Test
    void studentExceptionDiscardsIncompleteBatch() throws Exception {
        String source = """
                import com.antcolonywars.sdk.Bot;

                public final class FailingMain {
                    public static void main(Bot bot) {
                        bot.select("worker-blue");
                        bot.go(2);
                        throw new IllegalStateException("lỗi giữa batch");
                    }
                }
                """;
        BotCompiler compiler = new BotCompiler();
        try (BotCompilation compilation = compiler.compile("FailingMain", source);
             BotProcessRunner runner = new BotProcessRunner()) {
            BotRunResult result = runner.start(request(compilation, "blue-token", Duration.ofSeconds(10)))
                    .completion()
                    .get(15, TimeUnit.SECONDS);

            assertFalse(result.successful());
            assertTrue(result.standardError().contains("lỗi giữa batch"));
            try (GameClient observer = GameClient.connect(serverUrl(), "blue-token")) {
                assertEquals(0, observer.getLatestState().revision());
                assertEquals(1, observer.getLatestState().snapshot().units().stream()
                        .filter(unit -> unit.id().equals("worker-blue"))
                        .findFirst().orElseThrow().position().x());
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

    @Test
    void explicitMatchCodeIsPassedToChildJvm() throws Exception {
        BotCompiler compiler = new BotCompiler();
        try (BotCompilation compilation = compiler.compile("WrongMatchBot", BotCompilerTest.validSource("WrongMatchBot"));
             BotProcessRunner runner = new BotProcessRunner()) {
            BotRunResult result = runner.start(request(
                            compilation, "blue-token", "missing-match", Duration.ofSeconds(5)))
                    .completion()
                    .get(10, TimeUnit.SECONDS);

            assertEquals(4, result.exitCode());
            assertTrue(result.standardError().contains("CONNECTION_FAILED"));
        }
    }

    @Test
    void intellijSubmissionReceivesVietnameseServerErrorLog() throws Exception {
        try (StudentSubmissionServer submissions = new StudentSubmissionServer(
                ignored -> java.util.concurrent.CompletableFuture.completedFuture(
                        new StudentSubmissionResult(
                                false,
                                "Lệnh 1 — worker-blue — di chuyển phải: Ô đích đang bị chiếm."
                        )
                ),
                0
        )) {
            submissions.start();
            String body = """
                    {"className":"Main","source":"class Main {}","pairingKey":"123456","username":"student","password":"secret","batchMode":"ATOMIC"}
                    """;
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + submissions.port() + "/submit"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(422, response.statusCode());
            assertTrue(response.body().contains("Lệnh 1 — worker-blue — di chuyển phải"));
            assertTrue(response.body().contains("Ô đích đang bị chiếm"));
        }
    }

    @Test
    void oneIntellijSubmissionRunsThroughDesktopAndReturnsServerResult() throws Exception {
        try (StudentSubmissionServer submissions = new StudentSubmissionServer(submission ->
                java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    BotCompiler compiler = new BotCompiler();
                    try (BotCompilation compilation = compiler.compile(submission.className(), submission.source());
                         BotProcessRunner runner = new BotProcessRunner()) {
                        if (!compilation.successful()) {
                            return new StudentSubmissionResult(false, "Biên dịch thất bại.");
                        }
                        BotRunResult result = runner.start(request(
                                compilation, "blue-token", Duration.ofSeconds(10)
                        )).completion().get(15, TimeUnit.SECONDS);
                        String output = result.standardOutput() + result.standardError();
                        return new StudentSubmissionResult(result.successful(), output);
                    } catch (Exception exception) {
                        return new StudentSubmissionResult(false, exception.getMessage());
                    }
                }), 0)) {
            submissions.start();
            String source = """
                    import com.antcolonywars.sdk.Bot;
                    public final class Main {
                        public static void main(Bot bot) {
                            bot.select("worker-blue");
                            bot.go(2);
                        }
                    }
                    """;
            String body = new ObjectMapper().writeValueAsString(Map.of(
                    "className", "Main",
                    "source", source,
                    "pairingKey", "123456",
                    "username", "student",
                    "password", "secret",
                    "batchMode", "ATOMIC"
            ));

            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + submissions.port() + "/submit"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(200, response.statusCode());
            assertTrue(response.body().contains("Batch đã được server chấp nhận"));
            try (GameClient observer = GameClient.connect(serverUrl(), "blue-token")) {
                assertEquals(2, observer.getLatestState().snapshot().units().stream()
                        .filter(unit -> unit.id().equals("worker-blue"))
                        .findFirst().orElseThrow().position().x());
            }
        }
    }

    @Test
    void intellijSubmissionWithoutAccountIsRejectedBeforeRunning() throws Exception {
        java.util.concurrent.atomic.AtomicBoolean handled = new java.util.concurrent.atomic.AtomicBoolean();
        try (StudentSubmissionServer submissions = new StudentSubmissionServer(submission -> {
            handled.set(true);
            return java.util.concurrent.CompletableFuture.completedFuture(
                    new StudentSubmissionResult(true, "unexpected")
            );
        }, 0)) {
            submissions.start();
            String body = """
                    {"className":"Main","source":"class Main {}","pairingKey":"123456","username":"student","password":"","batchMode":"ATOMIC"}
                    """;

            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + submissions.port() + "/submit"))
                            .header("Content-Type", "application/json")
                            .POST(HttpRequest.BodyPublishers.ofString(body))
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(400, response.statusCode());
            assertTrue(response.body().contains("PASSWORD"));
            assertFalse(handled.get());
        }
    }

    private BotRunRequest request(BotCompilation compilation, String token, Duration timeout) {
        return request(compilation, token, "", timeout);
    }

    private BotRunRequest request(BotCompilation compilation, String token, String matchId, Duration timeout) {
        return new BotRunRequest(
                compilation.className(),
                compilation.classesDirectory(),
                serverUrl(),
                matchId,
                token,
                BatchMode.ATOMIC,
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
