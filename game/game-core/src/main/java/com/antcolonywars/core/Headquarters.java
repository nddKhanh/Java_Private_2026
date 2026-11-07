package com.antcolonywars.core;

import java.util.Objects;

public record Headquarters(String id, String teamId, Position position, int hp) {
    public Headquarters {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(position, "position");
        if (hp < 0) {
            throw new IllegalArgumentException("Headquarters hp cannot be negative");
        }
    }

    public Headquarters takeDamage(int damage) {
        return new Headquarters(id, teamId, position, Math.max(0, hp - damage));
    }
}
