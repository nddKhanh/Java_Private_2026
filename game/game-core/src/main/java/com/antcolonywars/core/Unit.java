package com.antcolonywars.core;

import java.util.Objects;

public record Unit(
        String id,
        String teamId,
        String type,
        Position position,
        int hp,
        Direction facing
) {
    public Unit {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(facing, "facing");
        if (hp < 0) {
            throw new IllegalArgumentException("Unit hp cannot be negative");
        }
    }

    public Unit moveTo(Position nextPosition, Direction nextFacing) {
        return new Unit(id, teamId, type, nextPosition, hp, nextFacing);
    }

    public Unit takeDamage(int damage) {
        return new Unit(id, teamId, type, position, Math.max(0, hp - damage), facing);
    }
}
