package com.antcolonywars.server;

import java.util.List;

/**
 * Reply to a COMMAND, BATCH, GET_STATE or CLEAR_QUEUE message, sent as soon as the server has queued it.
 * queuedCount counts the commands added to bot queues (or removed, for CLEAR_QUEUE);
 * rejections lists the commands refused on arrival.
 */
public record CommandResultMessage(
        String type,
        int schemaVersion,
        String matchId,
        String commandId,
        boolean ok,
        String error,
        String errorMessage,
        int queuedCount,
        List<RejectionMessage> rejections,
        long revision,
        long sequence
) {
    public CommandResultMessage {
        rejections = rejections == null ? List.of() : List.copyOf(rejections);
    }

    public static CommandResultMessage success(String matchId, String commandId, long revision, long sequence) {
        return queued(matchId, commandId, 0, List.of(), revision, sequence);
    }

    public static CommandResultMessage failure(
            String matchId,
            String commandId,
            String error,
            long revision,
            long sequence
    ) {
        return new CommandResultMessage(
                "COMMAND_RESULT", GameWebSocketHandler.SCHEMA_VERSION, matchId, commandId,
                false, error, ErrorMessages.vietnamese(error), 0, List.of(), revision, sequence
        );
    }

    public static CommandResultMessage queued(
            String matchId,
            String commandId,
            int queuedCount,
            List<RejectionMessage> rejections,
            long revision,
            long sequence
    ) {
        RejectionMessage first = rejections.isEmpty() ? null : rejections.getFirst();
        return new CommandResultMessage(
                "COMMAND_RESULT", GameWebSocketHandler.SCHEMA_VERSION, matchId, commandId,
                first == null, first == null ? null : first.error(), first == null ? null : first.errorMessage(),
                queuedCount, rejections, revision, sequence
        );
    }
}
