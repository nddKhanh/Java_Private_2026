package com.antcolonywars.desktop;

import java.util.concurrent.CompletableFuture;

final class RunGeneration {
    private long current;
    private CompletableFuture<StudentSubmissionResult> submission;

    synchronized long replace(CompletableFuture<StudentSubmissionResult> next) {
        if (submission != null && !submission.isDone()) {
            submission.complete(new StudentSubmissionResult(false, "Lần Run trước đã bị thay thế."));
        }
        current++;
        submission = next;
        return current;
    }

    synchronized long current() {
        return current;
    }

    synchronized boolean isCurrent(long generation) {
        return generation == current;
    }

    synchronized void complete(long generation, StudentSubmissionResult result) {
        if (generation != current) {
            return;
        }
        if (submission != null && !submission.isDone()) {
            submission.complete(result);
        }
        submission = null;
    }
}
