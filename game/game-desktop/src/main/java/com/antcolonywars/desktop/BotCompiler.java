package com.antcolonywars.desktop;

import com.antcolonywars.bot.BotProgram;
import com.antcolonywars.bot.GameBot;
import com.antcolonywars.client.Direction;
import com.antcolonywars.client.GameState;
import com.antcolonywars.core.MatchState;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class BotCompiler {
    private static final Pattern CLASS_NAME = Pattern.compile(
            "[A-Za-z_$][A-Za-z0-9_$]*(\\.[A-Za-z_$][A-Za-z0-9_$]*)*"
    );

    public BotCompilation compile(String className, String source) {
        if (className == null || !CLASS_NAME.matcher(className).matches()) {
            return failure(className, "Class name must be a valid qualified Java name.");
        }
        if (source == null || source.isBlank()) {
            return failure(className, "Source code cannot be empty.");
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return failure(className, "No Java compiler is available. Run the app with JDK 21.");
        }

        Path output;
        try {
            output = Files.createTempDirectory("ant-colony-bot-").toAbsolutePath().normalize();
        } catch (IOException exception) {
            return failure(className, "Cannot create a temporary compilation directory: " + exception.getMessage());
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.ROOT, null)) {
            fileManager.setLocationFromPaths(StandardLocation.CLASS_OUTPUT, List.of(output));
            List<String> options = List.of(
                    "--release", "21",
                    "-classpath", compilationClassPath()
            );
            JavaFileObject sourceFile = new SourceFile(className, source);
            boolean successful = Boolean.TRUE.equals(compiler.getTask(
                    null,
                    fileManager,
                    diagnostics,
                    options,
                    null,
                    List.of(sourceFile)
            ).call());
            List<CompilationMessage> messages = diagnostics.getDiagnostics().stream()
                    .map(BotCompiler::toMessage)
                    .toList();
            if (!successful) {
                deleteFailedOutput(output);
                return new BotCompilation(false, className, null, messages);
            }
            return new BotCompilation(true, className, output, messages);
        } catch (IOException | RuntimeException exception) {
            deleteFailedOutput(output);
            return failure(className, "Compilation failed: " + exception.getMessage());
        }
    }

    private static String compilationClassPath() {
        return ClassPathResolver.forClasses(
                BotProgram.class,
                GameBot.class,
                Direction.class,
                GameState.class,
                MatchState.class
        );
    }

    private static CompilationMessage toMessage(Diagnostic<? extends JavaFileObject> diagnostic) {
        CompilationMessage.Kind kind = switch (diagnostic.getKind()) {
            case ERROR -> CompilationMessage.Kind.ERROR;
            case WARNING, MANDATORY_WARNING -> CompilationMessage.Kind.WARNING;
            case NOTE -> CompilationMessage.Kind.NOTE;
            default -> CompilationMessage.Kind.OTHER;
        };
        return new CompilationMessage(
                kind,
                diagnostic.getLineNumber(),
                diagnostic.getColumnNumber(),
                diagnostic.getMessage(Locale.ROOT)
        );
    }

    private static BotCompilation failure(String className, String message) {
        return new BotCompilation(
                false,
                className,
                null,
                List.of(new CompilationMessage(CompilationMessage.Kind.ERROR, -1, -1, message))
        );
    }

    private static void deleteFailedOutput(Path output) {
        List<Path> paths = new ArrayList<>();
        try (var stream = Files.walk(output)) {
            stream.sorted((left, right) -> right.compareTo(left)).forEach(paths::add);
        } catch (IOException ignored) {
            return;
        }
        for (Path path : paths) {
            try {
                Files.deleteIfExists(path);
            } catch (IOException ignored) {
                return;
            }
        }
    }

    private static final class SourceFile extends SimpleJavaFileObject {
        private final String source;

        private SourceFile(String className, String source) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
