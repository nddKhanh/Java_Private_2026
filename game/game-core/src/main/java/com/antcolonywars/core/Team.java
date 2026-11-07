package com.antcolonywars.core;

import java.util.Objects;

public record Team(String id, String displayName, String color) {
    public Team {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(color, "color");
    }
}
