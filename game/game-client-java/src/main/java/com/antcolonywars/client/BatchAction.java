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
        String buildingType,
        String sourceId
) {
    public static BatchAction move(String unitId, Direction direction) {
        return action("MOVE", unitId, null, null, direction.name(), null, null, null);
    }

    public static BatchAction waitTurn(String unitId) {
        return action("WAIT", unitId, null, null, null, null, null, null);
    }

    public static BatchAction attack(String unitId, Direction direction) {
        return action("ATTACK", unitId, null, null, direction.name(), null, null, null);
    }

    public static BatchAction harvest(String unitId) {
        return action("HARVEST", unitId, null, null, null, null, null, null);
    }

    public static BatchAction build(
            String unitId,
            String buildingId,
            BuildingType buildingType,
            Direction direction
    ) {
        return action("BUILD", unitId, null, null, direction.name(), buildingId, buildingType.name(), null);
    }

    public static BatchAction spawn(
            String sourceId,
            String unitId,
            String displayName,
            String unitType,
            Direction direction
    ) {
        return action("SPAWN", unitId, displayName, unitType, direction.name(), null, null, sourceId);
    }

    public static BatchAction spawn(
            String unitId,
            String displayName,
            String unitType,
            Direction direction
    ) {
        return spawn(null, unitId, displayName, unitType, direction);
    }

    private static BatchAction action(
            String action,
            String unitId,
            String displayName,
            String unitType,
            String direction,
            String buildingId,
            String buildingType,
            String sourceId
    ) {
        return new BatchAction(
                UUID.randomUUID().toString(), action, unitId, displayName, unitType,
                direction, null, buildingId, buildingType, sourceId
        );
    }
}
