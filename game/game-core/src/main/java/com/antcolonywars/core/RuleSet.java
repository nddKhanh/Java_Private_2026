package com.antcolonywars.core;

import java.util.Objects;

public record RuleSet(String version, int attackDamage, int attackRange) {
    public RuleSet {
        Objects.requireNonNull(version, "version");
        if (attackDamage <= 0) {
            throw new IllegalArgumentException("Attack damage must be positive");
        }
        if (attackRange <= 0) {
            throw new IllegalArgumentException("Attack range must be positive");
        }
    }

    public static RuleSet mvp() {
        return new RuleSet("mvp-1", 1, 1);
    }
}
