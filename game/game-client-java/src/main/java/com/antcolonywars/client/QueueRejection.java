package com.antcolonywars.client;

/**
 * A command refused when it arrived (index is its position in the batch) or that failed on its turn (index is null).
 * droppedCount counts the later commands of the same bot in the same batch removed with it.
 */
public record QueueRejection(
        String commandId,
        String subjectId,
        String action,
        String error,
        String errorMessage,
        int droppedCount,
        Integer index
) {
}
