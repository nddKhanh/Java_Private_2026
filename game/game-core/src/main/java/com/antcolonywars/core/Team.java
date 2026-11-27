package com.antcolonywars.core;

import java.util.Objects;

public record Team(String id, String displayName, String color, String factionId, int wood, int meat) {
    public Team {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(color, "color");
        factionId = factionId == null || factionId.isBlank() ? id : factionId;
        if (wood < 0 || meat < 0) {
            throw new IllegalArgumentException("Team resources cannot be negative");
        }
    }

    public Team(String id, String displayName, String color) {
        this(id, displayName, color, id, 0, 0);
    }

    public Team(String id, String displayName, String color, String factionId) {
        this(id, displayName, color, factionId, 0, 0);
    }

    public Team withStock(int nextWood, int nextMeat) {
        return new Team(id, displayName, color, factionId, nextWood, nextMeat);
    }

    public Team gain(ResourceType type, int quantity) {
        return type == ResourceType.WOOD ? withStock(wood + quantity, meat) : withStock(wood, meat + quantity);
    }

    public Team spend(Cost cost) {
        return withStock(wood - cost.wood(), meat - cost.meat());
    }
}
