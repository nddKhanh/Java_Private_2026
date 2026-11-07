package com.antcolonywars.desktop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public final class BotCompilation implements AutoCloseable {
    private final boolean successful;
    private final String className;
    private final Path classesDirectory;
    private final List<CompilationMessage> messages;

    BotCompilation(
            boolean successful,
            String className,
            Path classesDirectory,
            List<CompilationMessage> messages
    ) {
        this.successful = successful;
        this.className = className;
        this.classesDirectory = classesDirectory;
        this.messages = List.copyOf(messages);
    }

    public boolean successful() {
        return successful;
    }

    public String className() {
        return className;
    }

    public Path classesDirectory() {
        return classesDirectory;
    }

    public List<CompilationMessage> messages() {
        return messages;
    }

    @Override
    public void close() {
        if (classesDirectory == null || !Files.exists(classesDirectory)) {
            return;
        }
        try (var paths = Files.walk(classesDirectory)) {
            paths.sorted(Comparator.reverseOrder()).forEach(BotCompilation::delete);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot clean compilation directory", exception);
        }
    }

    private static void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot delete " + path, exception);
        }
    }
}
