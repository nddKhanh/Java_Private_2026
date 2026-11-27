package com.antcolonywars.server;

import com.antcolonywars.core.CommandRejection;

/**
 * A refused or failed command reported to its own team.
 * index is the position in the sent batch when the command was refused on arrival, and null when it failed on its turn;
 * droppedCount counts the later commands of the same bot in the same batch removed with it.
 */
public record RejectionMessage(
        String batchId,
        String commandId,
        String subjectId,
        String action,
        String error,
        String errorMessage,
        int droppedCount,
        Integer index
) {
    public static RejectionMessage onArrival(
            String batchId, int index, String commandId, String subjectId, String action, String error, int droppedCount
    ) {
        return new RejectionMessage(
                batchId, commandId, subjectId, action, error, ErrorMessages.vietnamese(error), droppedCount, index
        );
    }

    public static RejectionMessage onTurn(CommandRejection rejection) {
        String error = rejection.error().name();
        return new RejectionMessage(
                rejection.batchId(), rejection.commandId(), rejection.subjectId(), rejection.action(), error,
                ErrorMessages.vietnamese(error), rejection.droppedCount(), null
        );
    }
}
