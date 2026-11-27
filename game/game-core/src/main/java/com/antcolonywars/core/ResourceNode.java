package com.antcolonywars.core;

import java.util.Objects;

public record ResourceNode(String id, ResourceType type, Position position, int amount) {
    public ResourceNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(position, "position");
        type = type == null ? ResourceType.WOOD : type;
        if (amount < 0) {
            throw new IllegalArgumentException("Resource amount cannot be negative");
        }
    }

    public ResourceNode(String id, Position position, int amount) {
        this(id, ResourceType.WOOD, position, amount);
    }

    public ResourceNode harvest(int quantity) {
        return new ResourceNode(id, type, position, Math.max(0, amount - quantity));
    }
}
