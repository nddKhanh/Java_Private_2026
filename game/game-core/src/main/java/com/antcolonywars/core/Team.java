package com.antcolonywars.core;

import java.util.Objects;

public record Team(String id, String displayName, String color, String factionId, int resources) {
    public Team {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(color, "color");
        factionId = factionId == null || factionId.isBlank() ? id : factionId;
        if (resources < 0) {
            throw new IllegalArgumentException("Team resources cannot be negative");
        }
    }

    public Team(String id, String displayName, String color, int resources) {
        this(id, displayName, color, id, resources);
    }

    public Team(String id, String displayName, String color) {
        this(id, displayName, color, id, 0);
    }

    public Team(String id, String displayName, String color, String factionId) {
        this(id, displayName, color, factionId, 0);
    }

    public Team withResources(int nextResources) {
        return new Team(id, displayName, color, factionId, nextResources);
    }
}
