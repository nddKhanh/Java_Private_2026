package com.antcolonywars.server;

public record BatchCommandMessage(
        String commandId,
        String action,
        String unitId,
        String displayName,
        String unitType,
        String direction,
        String targetId,
        String buildingId,
        String buildingType
) {
}
