package com.antcolonywars.core;

import java.util.Objects;

public record ResourceNode(String id, Position position, int amount) {
    public ResourceNode {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(position, "position");
        if (amount < 0) {
            throw new IllegalArgumentException("Resource amount cannot be negative");
        }
    }

    public ResourceNode harvest(int quantity) {
        return new ResourceNode(id, position, Math.max(0, amount - quantity));
    }
}
