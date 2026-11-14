package com.antcolonywars.desktop;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunGenerationTest {
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
