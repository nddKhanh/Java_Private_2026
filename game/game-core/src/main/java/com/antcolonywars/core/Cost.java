package com.antcolonywars.core;

public record Cost(int wood, int meat) {
    public Cost {
        if (wood < 0 || meat < 0) {
            throw new IllegalArgumentException("Cost cannot be negative");
        }
    }

    public boolean affordableBy(Team team) {
        return team.wood() >= wood && team.meat() >= meat;
    }
}
