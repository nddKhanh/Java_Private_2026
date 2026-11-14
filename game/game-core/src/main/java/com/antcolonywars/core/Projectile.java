package com.antcolonywars.core;

import java.util.Objects;

public record Projectile(
        String id,
        String teamId,
        String sourceUnitId,
        String targetId,
        Position position,
        Position targetPosition,
        int damage
) {
    public Projectile {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(sourceUnitId, "sourceUnitId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(targetPosition, "targetPosition");
        if (damage <= 0) {
            throw new IllegalArgumentException("Projectile damage must be positive");
        }
    }

    public Projectile moveTo(Position nextPosition) {
        return new Projectile(id, teamId, sourceUnitId, targetId, nextPosition, targetPosition, damage);
    }
}
