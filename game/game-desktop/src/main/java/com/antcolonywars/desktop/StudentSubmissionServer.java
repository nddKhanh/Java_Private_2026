package com.antcolonywars.desktop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

final class StudentSubmissionServer implements AutoCloseable {
    static final int PORT = 17321;
    private static final int MAX_SOURCE_BYTES = 512 * 1024;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Function<StudentSubmission, CompletableFuture<StudentSubmissionResult>> handler;
    private final int requestedPort;
    private HttpServer server;
    private ExecutorService executor;

    StudentSubmissionServer(Function<StudentSubmission, CompletableFuture<StudentSubmissionResult>> handler) {
        this(handler, PORT);
    }

    StudentSubmissionServer(
            Function<StudentSubmission, CompletableFuture<StudentSubmissionResult>> handler,
            int requestedPort
    ) {
        this.handler = Objects.requireNonNull(handler, "handler");
        this.requestedPort = requestedPort;
    }

    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", requestedPort), 0);
        server.createContext("/submit", this::handle);
        executor = Executors.newVirtualThreadPerTaskExecutor();
        server.setExecutor(executor);
        server.start();
    }

    int port() {
        if (server == null) {
            throw new IllegalStateException("Submission server is not running");
        }
        return server.getAddress().getPort();
    }

    @Override
    public void close() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
        if (executor != null) {
            executor.close();
            executor = null;
        }
    }

    private void handle(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            respond(exchange, 405, "Chỉ hỗ trợ POST.");
            return;
        }
        byte[] body = exchange.getRequestBody().readNBytes(MAX_SOURCE_BYTES + 1);
        if (body.length > MAX_SOURCE_BYTES) {
            respond(exchange, 413, "Source vượt quá giới hạn 512 KiB.");
            return;
        }
        try {
            StudentSubmission submission = objectMapper.readValue(body, StudentSubmission.class);
            validate(submission);
            StudentSubmissionResult result = handler.apply(submission).get(150, TimeUnit.SECONDS);
            respond(exchange, result.successful() ? 200 : 422, result.output());
        } catch (Exception exception) {
            respond(exchange, 400, "Yêu cầu không hợp lệ: " + messageOf(exception));
        }
    }

    private static void validate(StudentSubmission submission) {
        if (submission == null
                || submission.className() == null || submission.className().isBlank()
                || submission.source() == null || submission.source().isBlank()
                || submission.pairingKey() == null || submission.pairingKey().isBlank()) {
            throw new IllegalArgumentException("Thiếu className, source hoặc pairingKey");
        }
    }

    private static void respond(HttpExchange exchange, int status, String text) throws IOException {
        byte[] body = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private static String messageOf(Throwable error) {
        String message = error.getMessage();
        return message == null || message.isBlank() ? error.getClass().getSimpleName() : message;
    }
}
