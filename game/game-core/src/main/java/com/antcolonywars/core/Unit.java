package com.antcolonywars.core;

import java.util.Objects;

public record Unit(
        String id,
        String displayName,
        String teamId,
        String type,
        Position position,
        int hp,
        Direction facing
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
    }

    public Unit(String id, String teamId, String type, Position position, int hp, Direction facing) {
        this(id, id, teamId, type, position, hp, facing);
    }

    public Unit moveTo(Position nextPosition, Direction nextFacing) {
        return new Unit(id, displayName, teamId, type, nextPosition, hp, nextFacing);
    }

    public Unit takeDamage(int damage) {
        return new Unit(id, displayName, teamId, type, position, Math.max(0, hp - damage), facing);
    }
}
