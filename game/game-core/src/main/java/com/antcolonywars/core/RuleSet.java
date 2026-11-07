package com.antcolonywars.core;

import java.util.Objects;

public record RuleSet(
        String version,
        int attackDamage,
        int attackRange,
        int maxUnitsPerTeam,
        int spawnedUnitHp
) {
    public RuleSet {
        Objects.requireNonNull(version, "version");
        if (attackDamage <= 0) {
            throw new IllegalArgumentException("Attack damage must be positive");
        }
        if (attackRange <= 0) {
            throw new IllegalArgumentException("Attack range must be positive");
        }
        if (maxUnitsPerTeam <= 0) {
            throw new IllegalArgumentException("Unit limit must be positive");
        }
        if (spawnedUnitHp <= 0) {
            throw new IllegalArgumentException("Spawned unit hp must be positive");
        }
    }

    public RuleSet(String version, int attackDamage, int attackRange) {
        this(version, attackDamage, attackRange, 5, 3);
    }

    public static RuleSet mvp() {
        return new RuleSet("r1-1", 1, 1, 5, 3);
    }
}
