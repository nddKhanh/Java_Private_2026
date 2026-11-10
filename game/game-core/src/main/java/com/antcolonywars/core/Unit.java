package com.antcolonywars.core;

import java.util.Objects;

public record Unit(
        String id,
        String displayName,
        String teamId,
        String type,
        Position position,
        int hp,
        Direction facing,
        int width,
        int height
) {
    public Unit {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(facing, "facing");
        if (hp < 0) {
            throw new IllegalArgumentException("Unit hp cannot be negative");
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Unit footprint must be positive");
        }
    }

    public Unit(String id, String teamId, String type, Position position, int hp, Direction facing) {
        this(id, id, teamId, type, position, hp, facing, defaultSize(type), defaultSize(type));
    }

    public Unit(
            String id,
            String displayName,
            String teamId,
            String type,
            Position position,
            int hp,
            Direction facing
    ) {
        this(id, displayName, teamId, type, position, hp, facing, defaultSize(type), defaultSize(type));
    }

    public Unit moveTo(Position nextPosition, Direction nextFacing) {
        return new Unit(id, displayName, teamId, type, nextPosition, hp, nextFacing, width, height);
    }

    public Unit takeDamage(int damage) {
        return new Unit(id, displayName, teamId, type, position, Math.max(0, hp - damage), facing, width, height);
    }

    private static int defaultSize(String type) {
        return UnitType.SIEGE.name().equals(type) ? 2 : 1;
    }
}
