package com.antcolonywars.desktop;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotCompilerTest {
    private final BotCompiler compiler = new BotCompiler();

    @Test
    void compilesBotProgramIntoAnIsolatedDirectory() {
        try (BotCompilation result = compiler.compile("SampleBot", validSource("SampleBot"))) {
            assertTrue(result.successful());
            assertTrue(Files.isRegularFile(result.classesDirectory().resolve("SampleBot.class")));
            assertTrue(result.messages().stream().noneMatch(message -> message.kind() == CompilationMessage.Kind.ERROR));
        }
    }

    @Test
    void reportsLineAndColumnForInvalidSourceAndKeepsNoRunnableOutput() {
        BotCompilation result = compiler.compile("BrokenBot", "public final class BrokenBot { invalid }");

        assertFalse(result.successful());
        assertNull(result.classesDirectory());
        assertEquals(CompilationMessage.Kind.ERROR, result.messages().getFirst().kind());
        assertTrue(result.messages().getFirst().line() > 0);
        assertTrue(result.messages().getFirst().column() > 0);
    }

    static String validSource(String className) {
        return """
                import com.antcolonywars.bot.BotProgram;
                import com.antcolonywars.bot.GameBot;
                import com.antcolonywars.client.Direction;

                public final class %s implements BotProgram {
                    public void run(GameBot bot) {
                        for (int step = 0; step < 3; step++) {
                            bot.move(Direction.RIGHT);
                        }
                        bot.awaitSimulationIdle();
                        System.out.println("moves-complete");
                    }
                }
                """.formatted(className);
    }
}
