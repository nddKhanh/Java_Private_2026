package com.antcolonywars.core;

import java.util.Objects;

public record RuleSet(
        String version,
        int attackDamage,
        int attackRange,
        int maxUnitsPerTeam,
        int spawnedUnitHp,
        int startingResources,
        int harvestAmount,
        int harvestRange,
        int barracksCost,
        int towerCost,
        int wallCost,
        int buildingHp
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
        if (startingResources < 0 || harvestAmount <= 0 || harvestRange < 0
                || barracksCost <= 0 || towerCost <= 0 || wallCost <= 0 || buildingHp <= 0) {
            throw new IllegalArgumentException("Invalid resource or building rule");
        }
    }

    public RuleSet(String version, int attackDamage, int attackRange) {
        this(version, attackDamage, attackRange, 5, 3, 0, 3, 0, 3, 2, 1, 5);
    }

    public RuleSet(String version, int attackDamage, int attackRange, int maxUnitsPerTeam, int spawnedUnitHp) {
        this(version, attackDamage, attackRange, maxUnitsPerTeam, spawnedUnitHp, 0, 3, 0, 3, 2, 1, 5);
    }

    public static RuleSet mvp() {
        return new RuleSet("r2-1", 1, 1, 5, 3, 0, 3, 0, 3, 2, 1, 5);
    }
}
