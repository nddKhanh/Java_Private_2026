package com.antcolonywars.server;

import java.util.List;

public record ClientMessage(
        String type,
        int schemaVersion,
        String matchId,
        String commandId,
        String action,
        String unitId,
        String displayName,
        String unitType,
        String direction,
        String targetId,
        String buildingId,
        String buildingType,
        String mode,
        List<BatchCommandMessage> commands
) {
}
