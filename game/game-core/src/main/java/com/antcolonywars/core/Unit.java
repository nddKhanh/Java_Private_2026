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
        int height,
        int maxHp
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
        if (maxHp < 1 || hp > maxHp) {
            throw new IllegalArgumentException("Unit maxHp must be positive and at least hp");
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Unit footprint must be positive");
        }
    }

    /** A newly spawned unit at full health: its max HP is the HP it starts with. */
    public Unit(
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
        this(id, displayName, teamId, type, position, hp, facing, width, height, Math.max(1, hp));
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
        return new Unit(id, displayName, teamId, type, nextPosition, hp, nextFacing, width, height, maxHp);
    }

    public Unit takeDamage(int damage) {
        return new Unit(id, displayName, teamId, type, position, Math.max(0, hp - damage), facing, width, height, maxHp);
    }

    private static int defaultSize(String type) {
        return UnitType.SIEGE.name().equals(type) ? 2 : 1;
    }
}
