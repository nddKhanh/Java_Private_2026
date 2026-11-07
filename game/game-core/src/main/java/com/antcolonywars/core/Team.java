package com.antcolonywars.core;

import java.util.Objects;

public record Team(String id, String displayName, String color, int resources) {
    public Team {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(color, "color");
        if (resources < 0) {
            throw new IllegalArgumentException("Team resources cannot be negative");
        }
    }

    public Team(String id, String displayName, String color) {
        this(id, displayName, color, 0);
    }

    public Team withResources(int nextResources) {
        return new Team(id, displayName, color, nextResources);
    }
}
