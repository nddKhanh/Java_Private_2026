package com.antcolonywars.core;

import java.util.Objects;

public record Headquarters(String id, String teamId, Position position, int hp, int maxHp) {
    public Headquarters {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(position, "position");
        if (hp < 0) {
            throw new IllegalArgumentException("Headquarters hp cannot be negative");
        }
        if (maxHp < 1 || hp > maxHp) {
            throw new IllegalArgumentException("Headquarters maxHp must be positive and at least hp");
        }
    }

    /** A newly placed headquarters at full health: its max HP is the HP it starts with. */
    public Headquarters(String id, String teamId, Position position, int hp) {
        this(id, teamId, position, hp, Math.max(1, hp));
    }

    public Headquarters takeDamage(int damage) {
        return new Headquarters(id, teamId, position, Math.max(0, hp - damage), maxHp);
    }
}
