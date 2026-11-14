package com.antcolonywars.desktop;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RunGenerationTest {
    @Test
    void resolvesEachIntellijSubmissionToItsRequestedTeamToken() {
        PairingConfiguration configuration = new PairingConfiguration(
                "ws://localhost:8080/game",
                "MATCH-1",
                "OPEN",
                List.of(
                        new PairingConfiguration.PairingTeam("BLUE", "Blue", "blue-token"),
                        new PairingConfiguration.PairingTeam("RED", "Red", "red-token")
                )
        );

        assertEquals("blue-token", CodeStudioApplication.teamTokenFor(configuration, "BLUE"));
        assertEquals("red-token", CodeStudioApplication.teamTokenFor(configuration, "RED"));
        IllegalArgumentException missing = assertThrows(
                IllegalArgumentException.class,
                () -> CodeStudioApplication.teamTokenFor(configuration, "")
        );
        IllegalArgumentException unknown = assertThrows(
                IllegalArgumentException.class,
                () -> CodeStudioApplication.teamTokenFor(configuration, "GREEN")
        );
        assertTrue(missing.getMessage().contains("Thiếu TEAM_ID"));
        assertTrue(unknown.getMessage().contains("Không tìm thấy TEAM_ID 'GREEN'"));
    }

    @Test
    void ignoresLatePairingAndCompilationCallbacksFromTheReplacedRun() {
        RunGeneration generation = new RunGeneration();
        CompletableFuture<StudentSubmissionResult> submissionA = new CompletableFuture<>();
        CompletableFuture<StudentSubmissionResult> submissionB = new CompletableFuture<>();
        long runA = generation.replace(submissionA);
        CompletableFuture<String> pairingA = new CompletableFuture<>();
        CompletableFuture<String> compilationA = new CompletableFuture<>();
        AtomicReference<String> activeConfiguration = new AtomicReference<>();
        AtomicReference<String> activeSource = new AtomicReference<>();
        pairingA.thenAccept(value -> {
            if (generation.isCurrent(runA)) {
                activeConfiguration.set(value);
            }
        });
        compilationA.thenAccept(value -> {
            if (generation.isCurrent(runA)) {
                activeSource.set(value);
            }
        });

        long runB = generation.replace(submissionB);
        pairingA.complete("match-a|token-a");
        compilationA.complete("source-a");
        if (generation.isCurrent(runB)) {
            activeConfiguration.set("match-b|token-b");
            activeSource.set("source-b");
        }

        assertTrue(submissionA.isDone());
        assertFalse(submissionA.join().successful());
        assertEquals("match-b|token-b", activeConfiguration.get());
        assertEquals("source-b", activeSource.get());
        assertFalse(submissionB.isDone());
    }

    @Test
    void onlyTheCurrentRunCanCompleteTheActiveSubmission() {
        RunGeneration generation = new RunGeneration();
        CompletableFuture<StudentSubmissionResult> submissionA = new CompletableFuture<>();
        CompletableFuture<StudentSubmissionResult> submissionB = new CompletableFuture<>();
        long runA = generation.replace(submissionA);
        long runB = generation.replace(submissionB);

        generation.complete(runA, new StudentSubmissionResult(true, "old batch"));
        generation.complete(runB, new StudentSubmissionResult(true, "current batch"));

        assertFalse(submissionA.join().successful());
        assertTrue(submissionB.join().successful());
        assertEquals("current batch", submissionB.join().output());
    }
}
