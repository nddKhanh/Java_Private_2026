package com.antcolonywars.core;

import java.util.Objects;

public record Building(
        String id,
        String teamId,
        BuildingType type,
        Position position,
        int hp,
        int maxHp
) {
    public Building {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(position, "position");
        if (hp < 0) {
            throw new IllegalArgumentException("Building hp cannot be negative");
        }
        if (maxHp < 1 || hp > maxHp) {
            throw new IllegalArgumentException("Building maxHp must be positive and at least hp");
        }
    }

    /** A newly built building at full health: its max HP is the HP it starts with. */
    public Building(String id, String teamId, BuildingType type, Position position, int hp) {
        this(id, teamId, type, position, hp, Math.max(1, hp));
    }

    public Building takeDamage(int damage) {
        return new Building(id, teamId, type, position, Math.max(0, hp - damage), maxHp);
    }
}
