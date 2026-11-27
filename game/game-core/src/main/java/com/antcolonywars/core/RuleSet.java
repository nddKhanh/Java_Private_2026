package com.antcolonywars.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Balance values of a match; every number comes from the server rules file. */
public record RuleSet(
        String version,
        int maxUnitsPerTeam,
        int startingWood,
        int startingMeat,
        int harvestAmount,
        int harvestRange,
        int headquartersHp,
        Map<UnitType, UnitStats> units,
        Map<BuildingType, BuildingStats> buildings,
        TowerStats tower
) {
    public RuleSet {
        Objects.requireNonNull(version, "version");
        Objects.requireNonNull(tower, "tower");
        if (maxUnitsPerTeam <= 0) {
            throw new IllegalArgumentException("Unit limit must be positive");
        }
        if (startingWood < 0 || startingMeat < 0 || harvestAmount <= 0 || harvestRange < 0 || headquartersHp <= 0) {
            throw new IllegalArgumentException("Invalid economy or headquarters rule");
        }
        units = complete(units, UnitType.class, "unit");
        buildings = complete(buildings, BuildingType.class, "building");
    }

    /** Uniform stats for tests: SIEGE adds 1 damage and a 2x2 footprint, RANGED adds 1 range. */
    public RuleSet(
            String version, int attackDamage, int attackRange, int maxUnitsPerTeam, int spawnedUnitHp,
            int startingWood, int startingMeat, int harvestAmount, int harvestRange, int barracksCost,
            int towerCost, int wallCost, int buildingHp, int towerCooldownTicks, int workerCost, int meleeCost,
            int rangedCost, int siegeMeatCost, int siegeWoodCost
    ) {
        this(version, maxUnitsPerTeam, startingWood, startingMeat, harvestAmount, harvestRange, 3,
                Map.of(
                        UnitType.WORKER, new UnitStats(spawnedUnitHp, attackDamage, attackRange, workerCost, 0, 1),
                        UnitType.MELEE, new UnitStats(spawnedUnitHp, attackDamage, attackRange, meleeCost, 0, 1),
                        UnitType.RANGED, new UnitStats(spawnedUnitHp, attackDamage, attackRange + 1, rangedCost, 0, 1),
                        UnitType.SIEGE, new UnitStats(
                                spawnedUnitHp, attackDamage + 1, attackRange, siegeMeatCost, siegeWoodCost, 2)
                ),
                Map.of(
                        BuildingType.BARRACKS, new BuildingStats(buildingHp, barracksCost),
                        BuildingType.TOWER, new BuildingStats(buildingHp, towerCost),
                        BuildingType.WALL, new BuildingStats(buildingHp, wallCost)
                ),
                new TowerStats(1, 5, towerCooldownTicks));
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

    public UnitStats unit(UnitType unitType) {
        return units.get(unitType);
    }

    public UnitStats unit(String unitType) {
        return unit(UnitType.valueOf(unitType));
    }

    public BuildingStats building(BuildingType buildingType) {
        return buildings.get(buildingType);
    }

    public Cost unitCost(UnitType unitType) {
        UnitStats stats = unit(unitType);
        return new Cost(stats.woodCost(), stats.meatCost());
    }

    public Cost buildingCost(BuildingType buildingType) {
        return new Cost(building(buildingType).woodCost(), 0);
    }

    public int attackDamageFor(String unitType) {
        return unit(unitType).damage();
    }

    public int attackRangeFor(String unitType) {
        return unit(unitType).range();
    }

    private static <K extends Enum<K>, V> Map<K, V> complete(Map<K, V> values, Class<K> type, String kind) {
        Objects.requireNonNull(values, kind + "s");
        Map<K, V> copy = new EnumMap<>(type);
        for (K key : type.getEnumConstants()) {
            V value = values.get(key);
            if (value == null) {
                throw new IllegalArgumentException("Missing " + kind + " stats: " + key);
            }
            copy.put(key, value);
        }
        return Map.copyOf(copy);
    }

    /** Stats of one unit type; size is the side of its square footprint. */
    public record UnitStats(int hp, int damage, int range, int meatCost, int woodCost, int size) {
        public UnitStats {
            if (hp <= 0 || damage <= 0 || range <= 0 || meatCost < 0 || woodCost < 0 || size <= 0) {
                throw new IllegalArgumentException("Invalid unit stats");
            }
        }
    }

    public record BuildingStats(int hp, int woodCost) {
        public BuildingStats {
            if (hp <= 0 || woodCost <= 0) {
                throw new IllegalArgumentException("Invalid building stats");
            }
        }
    }

    public record TowerStats(int damage, int range, int cooldownTicks) {
        public TowerStats {
            if (damage <= 0 || range <= 0 || cooldownTicks <= 0) {
                throw new IllegalArgumentException("Invalid tower stats");
            }
        }
    }
}
