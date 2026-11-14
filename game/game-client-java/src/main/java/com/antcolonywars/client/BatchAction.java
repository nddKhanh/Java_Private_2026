package com.antcolonywars.client;

import java.util.UUID;

public record BatchAction(
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
    public static BatchAction move(String unitId, Direction direction) {
        return action("MOVE", unitId, null, null, direction.name(), null, null);
    }

    public static BatchAction waitTurn(String unitId) {
        return action("WAIT", unitId, null, null, null, null, null);
    }

    public static BatchAction attack(String unitId, Direction direction) {
        return action("ATTACK", unitId, null, null, direction.name(), null, null);
    }

    public static BatchAction harvest(String unitId) {
        return action("HARVEST", unitId, null, null, null, null, null);
    }

    public static BatchAction build(
            String unitId,
            String buildingId,
            BuildingType buildingType,
            Direction direction
    ) {
        return action("BUILD", unitId, null, null, direction.name(), buildingId, buildingType.name());
    }

    public static BatchAction spawn(
            String unitId,
            String displayName,
            String unitType,
            Direction direction
    ) {
        return action("SPAWN", unitId, displayName, unitType, direction.name(), null, null);
    }

    private static BatchAction action(
            String action,
            String unitId,
            String displayName,
            String unitType,
            String direction,
            String buildingId,
            String buildingType
    ) {
        return new BatchAction(
                UUID.randomUUID().toString(), action, unitId, displayName, unitType,
                direction, null, buildingId, buildingType
        );
    }
}
