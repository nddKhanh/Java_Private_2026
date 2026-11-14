package com.antcolonywars.desktop;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CodeStudioApplication extends Application {
    private static final int MAX_OUTPUT_BYTES = 64 * 1024;

    private final BotCompiler compiler = new BotCompiler();
    private final BotProcessRunner processRunner = new BotProcessRunner();
    private final LanPairingClient pairingClient = new LanPairingClient();
    private final ExecutorService background = Executors.newVirtualThreadPerTaskExecutor();
    private final TextField pairingKey = new TextField();
    private final TextField serverUrl = new TextField("ws://localhost:8080/game");
    private final TextField matchId = new TextField();
    private final ComboBox<String> teamToken = new ComboBox<>();
    private final TextField className = new TextField(DefaultBotSource.CLASS_NAME);
    private final Spinner<Integer> timeoutSeconds = new Spinner<>(1, 300, 15);
    private final TextArea editor = new TextArea(DefaultBotSource.SOURCE);
    private final TextArea console = new TextArea();
    private final Label status = new Label("Ready");
    private final Button compileButton = new Button("Compile");
    private final Button pairButton = new Button("Pair");
    private final Button runButton = new Button("Run");
    private final Button stopButton = new Button("Stop");
    private BotCompilation activeCompilation;
    private RunningBot activeProcess;

    @Override
    public void start(Stage stage) {
        pairingKey.setPromptText("6-character pairing key");
        teamToken.setPromptText("Team token");
        teamToken.setEditable(true);
        matchId.setPromptText("Match code (blank = match-default)");
        editor.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");
        console.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12px;");
        console.setEditable(false);
        console.setWrapText(true);
        console.setPrefRowCount(8);
        runButton.setDisable(true);
        stopButton.setDisable(true);

        editor.textProperty().addListener((observable, oldValue, newValue) -> invalidateCompilation());
        className.textProperty().addListener((observable, oldValue, newValue) -> invalidateCompilation());
        compileButton.setOnAction(event -> compileSource());
        pairButton.setOnAction(event -> pairWithServer());
        runButton.setOnAction(event -> runBot());
        stopButton.setOnAction(event -> stopBot());

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setTop(connectionPanel());
        root.setCenter(editor);
        root.setBottom(bottomPanel());
        BorderPane.setMargin(editor, new Insets(12, 0, 12, 0));

        stage.setTitle("Ant Colony Code Studio");
        Scene scene = new Scene(root, 1050, 760);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN),
                compileButton::fire
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN),
                runButton::fire
        );
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN),
                stopButton::fire
        );
        stage.setScene(scene);
        stage.setMinWidth(760);
        stage.setMinHeight(560);
        stage.setOnCloseRequest(event -> shutdown());
        stage.show();
    }

    @Override
    public void stop() {
        shutdown();
    }

    private VBox connectionPanel() {
        HBox pairing = new HBox(
                8,
                new Label("Pairing key"), pairingKey, pairButton
        );
        HBox.setHgrow(pairingKey, Priority.ALWAYS);

        HBox connection = new HBox(
                8,
                new Label("Server"), serverUrl,
                new Label("Timeout"), timeoutSeconds,
                new Label("seconds")
        );
        HBox.setHgrow(serverUrl, Priority.ALWAYS);

        HBox match = new HBox(
                8,
                new Label("Match code"), matchId,
                new Label("Team token"), teamToken
        );
        HBox.setHgrow(matchId, Priority.ALWAYS);
        HBox.setHgrow(teamToken, Priority.ALWAYS);

        HBox source = new HBox(8, new Label("Bot class"), className);
        HBox.setHgrow(className, Priority.ALWAYS);
        return new VBox(8, pairing, connection, match, source);
    }

    private void pairWithServer() {
        String key = pairingKey.getText().trim();
        pairButton.setDisable(true);
        status.setText("Searching LAN...");
        CompletableFuture.supplyAsync(() -> pairingClient.find(key), background)
                .whenComplete((result, error) -> Platform.runLater(() -> finishPairing(result, error)));
    }

    private void finishPairing(PairingConfiguration result, Throwable error) {
        pairButton.setDisable(false);
        if (error != null) {
            status.setText("Pairing failed");
            appendConsole("Pairing error: " + messageOf(error));
            return;
        }
        serverUrl.setText(result.serverUrl());
        matchId.setText(result.matchCode());
        teamToken.getItems().setAll(result.teams().stream().map(PairingConfiguration.PairingTeam::token).toList());
        if (!teamToken.getItems().isEmpty()) {
            teamToken.getSelectionModel().selectFirst();
        }
        status.setText("Paired");
        appendConsole("Paired with " + result.matchCode() + ". Select a team token, then compile and run.");
    }

    private VBox bottomPanel() {
        HBox actions = new HBox(8, compileButton, runButton, stopButton, new Separator(Orientation.VERTICAL), status);
        return new VBox(8, actions, console);
    }

    private void compileSource() {
        setBusy("Compiling...");
        console.clear();
        String requestedClass = className.getText().trim();
        String requestedSource = editor.getText();
        CompletableFuture.supplyAsync(() -> compiler.compile(requestedClass, requestedSource), background)
                .whenComplete((result, error) -> Platform.runLater(() -> finishCompilation(result, error)));
    }

    private void finishCompilation(BotCompilation result, Throwable error) {
        compileButton.setDisable(false);
        if (error != null) {
            status.setText("Compile failed");
            appendConsole("Compiler error: " + messageOf(error));
            return;
        }
        closeCompilation();
        activeCompilation = result;
        for (CompilationMessage message : result.messages()) {
            appendConsole(format(message));
        }
        if (result.successful()) {
            status.setText("Compiled");
            runButton.setDisable(false);
            if (result.messages().isEmpty()) {
                appendConsole("Compilation successful.");
            }
        } else {
            status.setText("Compile failed");
            runButton.setDisable(true);
        }
    }

    private void runBot() {
        if (activeCompilation == null || !activeCompilation.successful()) {
            appendConsole("Compile the source before running it.");
            return;
        }
        String selectedTeamToken = teamToken.getEditor().getText().trim();
        if (serverUrl.getText().isBlank() || selectedTeamToken.isBlank()) {
            appendConsole("Server URL and team token are required.");
            return;
        }

        BotRunRequest request = new BotRunRequest(
                activeCompilation.className(),
                activeCompilation.classesDirectory(),
                serverUrl.getText().trim(),
                matchId.getText().trim(),
                selectedTeamToken,
                Duration.ofSeconds(timeoutSeconds.getValue()),
                64,
                MAX_OUTPUT_BYTES
        );
        status.setText("Starting...");
        compileButton.setDisable(true);
        runButton.setDisable(true);
        CompletableFuture.supplyAsync(() -> startProcess(request), background)
                .whenComplete((process, error) -> Platform.runLater(() -> processStarted(process, error)));
    }

    private RunningBot startProcess(BotRunRequest request) {
        try {
            return processRunner.start(request);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot start child JVM", exception);
        }
    }

    private void processStarted(RunningBot process, Throwable error) {
        if (error != null) {
            status.setText("Start failed");
            compileButton.setDisable(false);
            runButton.setDisable(false);
            appendConsole("Start error: " + messageOf(error));
            return;
        }
        activeProcess = process;
        status.setText("Running");
        stopButton.setDisable(false);
        process.completion().whenComplete((result, failure) -> Platform.runLater(
                () -> processFinished(process, result, failure)
        ));
    }

    private void processFinished(RunningBot process, BotRunResult result, Throwable error) {
        if (activeProcess == process) {
            activeProcess = null;
        }
        compileButton.setDisable(false);
        runButton.setDisable(activeCompilation == null || !activeCompilation.successful());
        stopButton.setDisable(true);
        if (error != null) {
            status.setText("Runner failed");
            appendConsole("Runner error: " + messageOf(error));
            return;
        }
        appendOutput("stdout", result.standardOutput());
        appendOutput("stderr", result.standardError());
        if (result.timedOut()) {
            status.setText("Timed out");
        } else if (result.stopped()) {
            status.setText("Stopped");
        } else if (result.successful()) {
            status.setText("Completed");
        } else {
            status.setText("Failed (exit " + result.exitCode() + ")");
        }
    }

    private void stopBot() {
        RunningBot process = activeProcess;
        if (process != null) {
            status.setText("Stopping...");
            process.stop();
        }
    }

    private void invalidateCompilation() {
        if (activeCompilation == null) {
            return;
        }
        closeCompilation();
        runButton.setDisable(true);
        status.setText("Source changed; compile again");
    }

    private void closeCompilation() {
        if (activeCompilation != null) {
            activeCompilation.close();
            activeCompilation = null;
        }
    }

    private void setBusy(String text) {
        status.setText(text);
        compileButton.setDisable(true);
        runButton.setDisable(true);
    }

    private void appendOutput(String stream, String text) {
        if (text != null && !text.isBlank()) {
            appendConsole("[" + stream + "]" + System.lineSeparator() + text.stripTrailing());
        }
    }

    private void appendConsole(String text) {
        if (!console.getText().isEmpty()) {
            console.appendText(System.lineSeparator());
        }
        console.appendText(text);
    }

    private static String format(CompilationMessage message) {
        String position = message.line() > 0
                ? " line " + message.line() + ", column " + message.column()
                : "";
        return message.kind() + position + ": " + message.message();
    }

    private static String messageOf(Throwable error) {
        Throwable current = error;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }

    private void shutdown() {
        if (activeProcess != null) {
            activeProcess.stop();
            activeProcess = null;
        }
        closeCompilation();
        processRunner.close();
        background.close();
    }
}
