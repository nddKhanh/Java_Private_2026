package com.antcolonywars.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Entities every team receives around its headquarters.
 * Offsets are relative to the headquarters and mirrored on the far half of each axis so they point inward.
 */
public record StarterPack(List<PackResource> resources, List<PackBuilding> buildings, List<PackUnit> units) {
    public StarterPack {
        resources = resources == null ? List.of() : List.copyOf(resources);
        buildings = buildings == null ? List.of() : List.copyOf(buildings);
        units = units == null ? List.of() : List.copyOf(units);
    }

    public static StarterPack empty() {
        return new StarterPack(List.of(), List.of(), List.of());
    }

    public Placement place(List<Team> teams, List<Headquarters> headquarters, int width, int height, RuleSet rules) {
        List<Unit> placedUnits = new ArrayList<>();
        List<ResourceNode> placedResources = new ArrayList<>();
        List<Building> placedBuildings = new ArrayList<>();
        for (Headquarters hq : headquarters) {
            Team team = teams.stream().filter(item -> item.id().equals(hq.teamId())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown team for headquarters: " + hq.id()));
            String suffix = team.id().toLowerCase(Locale.ROOT);
            int sx = hq.position().x() >= width / 2 ? -1 : 1;
            int sy = hq.position().y() >= height / 2 ? -1 : 1;
            Map<String, Integer> counters = new HashMap<>();
            for (PackResource resource : resources) {
                String prefix = resource.type().name().toLowerCase(Locale.ROOT);
                placedResources.add(new ResourceNode(
                        id(prefix, suffix, counters), resource.type(),
                        offset(hq, resource.dx(), resource.dy(), sx, sy), resource.amount()
                ));
            }
            for (PackBuilding building : buildings) {
                String prefix = "home-" + building.type().name().toLowerCase(Locale.ROOT);
                placedBuildings.add(new Building(
                        id(prefix, suffix, counters), team.id(), building.type(),
                        offset(hq, building.dx(), building.dy(), sx, sy), rules.buildingHp()
                ));
            }
            for (PackUnit unit : units) {
                String type = unit.type() == null ? UnitType.WORKER.name() : unit.type().name();
                String prefix = type.toLowerCase(Locale.ROOT);
                String unitId = id(prefix, suffix, counters);
                String displayName = team.displayName() + " " + prefix + " " + counters.get(prefix);
                placedUnits.add(new Unit(
                        unitId, displayName, team.id(), type, offset(hq, unit.dx(), unit.dy(), sx, sy),
                        rules.spawnedUnitHp(), sx > 0 ? Direction.RIGHT : Direction.LEFT
                ));
            }
        }
        return new Placement(placedUnits, placedResources, placedBuildings);
    }

    public MatchSetup applyTo(MatchSetup setup) {
        Placement placement = place(
                setup.teams(), setup.headquarters(), setup.mapWidth(), setup.mapHeight(), setup.rules()
        );
        return new MatchSetup(
                setup.setupId(), setup.matchId(), setup.mapWidth(), setup.mapHeight(), setup.rules(), setup.teams(),
                concat(placement.units(), setup.units()), setup.headquarters(),
                concat(placement.resources(), setup.resourceNodes()),
                concat(placement.buildings(), setup.buildings())
        );
    }

    private static <T> List<T> concat(List<T> first, List<T> second) {
        List<T> result = new ArrayList<>(first);
        result.addAll(second);
        return result;
    }

    private static Position offset(Headquarters hq, int dx, int dy, int sx, int sy) {
        return new Position(hq.position().x() + dx * sx, hq.position().y() + dy * sy);
    }

    private static String id(String prefix, String suffix, Map<String, Integer> counters) {
        int index = counters.merge(prefix, 1, Integer::sum);
        return index == 1 ? prefix + "-" + suffix : prefix + "-" + suffix + "-" + index;
    }

    public record PackResource(ResourceType type, int dx, int dy, int amount) {
    }

    public record PackBuilding(BuildingType type, int dx, int dy) {
    }

    public record PackUnit(UnitType type, int dx, int dy) {
    }

    public record Placement(List<Unit> units, List<ResourceNode> resources, List<Building> buildings) {
    }
}
