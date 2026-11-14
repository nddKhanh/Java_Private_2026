package com.antcolonywars.server;

public record CommandResultMessage(
        String type,
        int schemaVersion,
        String matchId,
        String commandId,
        boolean ok,
        String error,
        String errorMessage,
        Integer failedIndex,
        int appliedCount,
        long revision,
        long sequence
) {
    public static CommandResultMessage success(String matchId, String commandId, long revision, long sequence) {
        return new CommandResultMessage(
                "COMMAND_RESULT", GameWebSocketHandler.SCHEMA_VERSION, matchId, commandId,
                true, null, null, null, 1, revision, sequence
        );
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
                false, error, ErrorMessages.vietnamese(error), null, 0, revision, sequence
        );
    }

    public static CommandResultMessage batch(
            String matchId,
            String commandId,
            String error,
            Integer failedIndex,
            int appliedCount,
            long revision,
            long sequence
    ) {
        return new CommandResultMessage(
                "COMMAND_RESULT", GameWebSocketHandler.SCHEMA_VERSION, matchId, commandId,
                error == null, error, error == null ? null : ErrorMessages.vietnamese(error),
                failedIndex, appliedCount, revision, sequence
        );
    }
}
