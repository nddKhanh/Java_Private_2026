package com.antcolonywars.core;

import java.util.Objects;

public record Building(
        String id,
        String teamId,
        BuildingType type,
        Position position,
        int hp
) {
    public Building {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        if (hp < 0) {
            throw new IllegalArgumentException("Building hp cannot be negative");
        }
    }

    public Building takeDamage(int damage) {
        return new Building(id, teamId, type, position, Math.max(0, hp - damage));
    }
}
