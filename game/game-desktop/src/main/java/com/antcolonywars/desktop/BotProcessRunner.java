package com.antcolonywars.desktop;

import com.antcolonywars.bot.BotProgram;
import com.antcolonywars.client.GameClient;
import com.antcolonywars.core.MatchState;
import com.antcolonywars.runner.BotRunnerMain;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class BotProcessRunner implements AutoCloseable {
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public RunningBot start(BotRunRequest request) throws IOException {
        Path classesDirectory = request.classesDirectory().toAbsolutePath().normalize();
        if (!Files.isDirectory(classesDirectory)) {
            throw new IllegalArgumentException("Compiled classes directory does not exist");
        }

        ProcessBuilder builder = new ProcessBuilder(command(request, classesDirectory));
        builder.directory(classesDirectory.toFile());
        builder.environment().put("GAME_WS_URL", request.serverUrl());
        builder.environment().put("GAME_TEAM_TOKEN", request.teamToken());
        if (request.matchId().isBlank()) {
            builder.environment().remove("GAME_MATCH_ID");
        } else {
            builder.environment().put("GAME_MATCH_ID", request.matchId());
        }
        builder.environment().put(
                "GAME_CONNECTION_TIMEOUT_SECONDS",
                Long.toString(Math.max(1, Math.min(5, request.timeout().toSeconds())))
        );

        Process process = builder.start();
        AtomicBoolean stopped = new AtomicBoolean();
        AtomicBoolean timedOut = new AtomicBoolean();
        Future<String> stdout = executor.submit(() -> readLimited(process.getInputStream(), request.maxOutputBytes()));
        Future<String> stderr = executor.submit(() -> readLimited(process.getErrorStream(), request.maxOutputBytes()));
        CompletableFuture<BotRunResult> completion = CompletableFuture.supplyAsync(
                () -> waitFor(process, request.timeout(), stopped, timedOut, stdout, stderr),
                executor
        );
        return new RunningBot(process, completion, stopped);
    }

    @Override
    public void close() {
        executor.close();
    }

    private static List<String> command(BotRunRequest request, Path classesDirectory) {
        List<String> command = new ArrayList<>();
        command.add(javaExecutable().toString());
        command.add("-Xmx" + request.maxHeapMegabytes() + "m");
        command.add("-cp");
        command.add(classesDirectory + File.pathSeparator + runtimeClassPath());
        command.add(BotRunnerMain.class.getName());
        command.add(request.className());
        return command;
    }

    private static Path javaExecutable() {
        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Path path = Path.of(System.getProperty("java.home"), "bin", executable);
        if (!Files.isRegularFile(path)) {
            throw new IllegalStateException("Child JVM launcher is unavailable at " + path);
        }
        return path;
    }

    private static String runtimeClassPath() {
        return ClassPathResolver.forClasses(
                BotRunnerMain.class,
                BotProgram.class,
                GameClient.class,
                MatchState.class,
                ObjectMapper.class,
                JsonProcessingException.class,
                JsonCreator.class
        );
    }

    private static BotRunResult waitFor(
            Process process,
            Duration timeout,
            AtomicBoolean stopped,
            AtomicBoolean timedOut,
            Future<String> stdout,
            Future<String> stderr
    ) {
        try {
            if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                timedOut.set(true);
                process.destroyForcibly();
            }
            int exitCode = process.waitFor();
            return new BotRunResult(exitCode, timedOut.get(), stopped.get(), stdout.get(), stderr.get());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            throw new IllegalStateException("Interrupted while waiting for bot process", exception);
        } catch (ExecutionException exception) {
            process.destroyForcibly();
            throw new IllegalStateException("Cannot read bot process output", exception.getCause());
        }
    }

    private static String readLimited(InputStream input, int limit) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(limit, 8192));
        byte[] buffer = new byte[4096];
        int total = 0;
        boolean truncated = false;
        int read;
        while ((read = input.read(buffer)) != -1) {
            int remaining = limit - total;
            if (remaining > 0) {
                int accepted = Math.min(remaining, read);
                output.write(buffer, 0, accepted);
                total += accepted;
            }
            if (read > remaining) {
                truncated = true;
            }
        }
        String text = output.toString(StandardCharsets.UTF_8);
        return truncated ? text + System.lineSeparator() + "[output truncated]" : text;
    }
}
