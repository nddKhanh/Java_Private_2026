package com.antcolonywars.desktop;

import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PackageSmokeMain {
    private PackageSmokeMain() {
    }

    public static void main(String[] args) {
        if (ToolProvider.getSystemJavaCompiler() == null) {
            throw new IllegalStateException("Packaged runtime does not contain jdk.compiler");
        }

        try (BotCompilation compilation = new BotCompiler().compile(
                DefaultBotSource.CLASS_NAME,
                DefaultBotSource.SOURCE
        )) {
            if (!compilation.successful()) {
                throw new IllegalStateException("Packaged compiler smoke test failed: " + compilation.messages());
            }
        }

        String executable = System.getProperty("os.name").toLowerCase().contains("win") ? "java.exe" : "java";
        Path childJava = Path.of(System.getProperty("java.home"), "bin", executable);
        if (!Files.isRegularFile(childJava)) {
            throw new IllegalStateException("Packaged runtime cannot launch a child JVM");
        }

        System.out.println("Packaged compiler and child JVM are available.");
    }
}
