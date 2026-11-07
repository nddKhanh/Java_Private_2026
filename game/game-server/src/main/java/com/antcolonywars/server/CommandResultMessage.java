package com.antcolonywars.server;

public record CommandResultMessage(
        String type,
        int schemaVersion,
        String matchId,
        String commandId,
        boolean ok,
        String error,
        long revision,
        long sequence
) {
    public static CommandResultMessage success(String matchId, String commandId, long revision, long sequence) {
        return new CommandResultMessage(
                "COMMAND_RESULT", GameWebSocketHandler.SCHEMA_VERSION, matchId, commandId,
                true, null, revision, sequence
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
                false, error, revision, sequence
        );
    }
}
