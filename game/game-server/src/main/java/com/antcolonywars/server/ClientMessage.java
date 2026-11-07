package com.antcolonywars.server;

public record ClientMessage(
        String type,
        int schemaVersion,
        String matchId,
        String commandId,
        String action,
        String unitId,
        String direction,
        String targetId
) {
}
