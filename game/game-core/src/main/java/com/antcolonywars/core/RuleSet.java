package com.antcolonywars.core;

import java.util.Objects;

/** Units are paid in meat and buildings in wood; SIEGE pays both. */
public record RuleSet(
        String version,
        int attackDamage,
        int attackRange,
        int maxUnitsPerTeam,
        int spawnedUnitHp,
        int startingWood,
        int startingMeat,
        int harvestAmount,
        int harvestRange,
        int barracksCost,
        int towerCost,
        int wallCost,
        int buildingHp,
        int towerCooldownTicks,
        int workerCost,
        int meleeCost,
        int rangedCost,
        int siegeMeatCost,
        int siegeWoodCost
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
        if (startingWood < 0 || startingMeat < 0 || harvestAmount <= 0 || harvestRange < 0
                || barracksCost <= 0 || towerCost <= 0 || wallCost <= 0 || buildingHp <= 0
                || towerCooldownTicks <= 0 || workerCost < 0 || meleeCost < 0 || rangedCost < 0
                || siegeMeatCost < 0 || siegeWoodCost < 0) {
            throw new IllegalArgumentException("Invalid resource or building rule");
        }
    }

    public RuleSet(
            String version, int attackDamage, int attackRange, int maxUnitsPerTeam, int spawnedUnitHp,
            int startingResources, int harvestAmount, int harvestRange, int barracksCost, int towerCost,
            int wallCost, int buildingHp, int towerCooldownTicks
    ) {
        this(version, attackDamage, attackRange, maxUnitsPerTeam, spawnedUnitHp, startingResources,
                startingResources, harvestAmount, harvestRange, barracksCost, towerCost, wallCost, buildingHp,
                towerCooldownTicks, 0, 0, 0, 0, 0);
    }

    public RuleSet(String version, int attackDamage, int attackRange) {
        this(version, attackDamage, attackRange, 5, 3, 0, 3, 0, 3, 2, 1, 5, 4);
    }

    public RuleSet(String version, int attackDamage, int attackRange, int maxUnitsPerTeam, int spawnedUnitHp) {
        this(version, attackDamage, attackRange, maxUnitsPerTeam, spawnedUnitHp, 0, 3, 0, 3, 2, 1, 5, 4);
    }

    public static RuleSet mvp() {
        return new RuleSet("r2-1", 1, 1, 5, 3, 0, 3, 0, 3, 2, 1, 5, 4);
    }

    public Cost unitCost(UnitType unitType) {
        return switch (unitType) {
            case WORKER -> new Cost(0, workerCost);
            case MELEE -> new Cost(0, meleeCost);
            case RANGED -> new Cost(0, rangedCost);
            case SIEGE -> new Cost(siegeWoodCost, siegeMeatCost);
        };
    }

    public Cost buildingCost(BuildingType buildingType) {
        return switch (buildingType) {
            case BARRACKS -> new Cost(barracksCost, 0);
            case TOWER -> new Cost(towerCost, 0);
            case WALL -> new Cost(wallCost, 0);
        };
    }

    public int attackDamageFor(String unitType) {
        return UnitType.SIEGE.name().equals(unitType) ? attackDamage + 1 : attackDamage;
    }

    public int attackRangeFor(String unitType) {
        return UnitType.RANGED.name().equals(unitType) ? attackRange + 1 : attackRange;
    }
}
