package com.antcolonywars.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Entities every team receives around its headquarters.
 * Resource and building offsets are relative to the headquarters and mirrored on the far half of each axis
 * so they point inward; a unit without an offset takes a random free cell around the headquarters,
 * seeded by match and team.
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

    public MatchSetup applyTo(MatchSetup setup) {
        int width = setup.mapWidth();
        int height = setup.mapHeight();
        RuleSet rules = setup.rules();
        List<Unit> placedUnits = new ArrayList<>();
        List<ResourceNode> placedResources = new ArrayList<>();
        List<Building> placedBuildings = new ArrayList<>();
        Set<Position> occupied = new HashSet<>();
        setup.headquarters().forEach(hq -> occupied.add(hq.position()));
        setup.resourceNodes().forEach(node -> occupied.add(node.position()));
        setup.buildings().forEach(building -> occupied.add(building.position()));
        setup.units().forEach(unit -> occupied.addAll(cells(unit.position(), unit.width())));

        for (Headquarters hq : setup.headquarters()) {
            Team team = setup.teams().stream().filter(item -> item.id().equals(hq.teamId())).findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown team for headquarters: " + hq.id()));
            String suffix = team.id().toLowerCase(Locale.ROOT);
            int sx = hq.position().x() >= width / 2 ? -1 : 1;
            int sy = hq.position().y() >= height / 2 ? -1 : 1;
            Map<String, Integer> counters = new HashMap<>();
            for (PackResource resource : resources) {
                String prefix = resource.type().name().toLowerCase(Locale.ROOT);
                Position position = offset(hq, resource.dx(), resource.dy(), sx, sy);
                claim(position, team, width, height, occupied);
                placedResources.add(new ResourceNode(
                        id(prefix, suffix, counters), resource.type(), position, resource.amount()
                ));
            }
            for (PackBuilding building : buildings) {
                String prefix = "home-" + building.type().name().toLowerCase(Locale.ROOT);
                Position position = offset(hq, building.dx(), building.dy(), sx, sy);
                claim(position, team, width, height, occupied);
                placedBuildings.add(new Building(
                        id(prefix, suffix, counters), team.id(), building.type(), position,
                        rules.building(building.type()).hp()
                ));
            }
            Random random = new Random((setup.matchId() + ":" + team.id()).hashCode());
            for (PackUnit unit : units) {
                UnitType type = unit.type() == null ? UnitType.WORKER : unit.type();
                RuleSet.UnitStats stats = rules.unit(type);
                String prefix = type.name().toLowerCase(Locale.ROOT);
                String unitId = id(prefix, suffix, counters);
                String displayName = team.displayName() + " " + prefix + " " + counters.get(prefix);
                Position position = unit.dx() != null && unit.dy() != null
                        ? offset(hq, unit.dx(), unit.dy(), sx, sy)
                        : randomCellNear(hq.position(), stats.size(), width, height, occupied, random);
                if (position == null) {
                    throw new IllegalArgumentException("No free cell around headquarters of team " + team.id()
                            + " for starter unit " + unitId);
                }
                occupied.addAll(cells(position, stats.size()));
                placedUnits.add(new Unit(
                        unitId, displayName, team.id(), type.name(), position, stats.hp(),
                        sx > 0 ? Direction.RIGHT : Direction.LEFT, stats.size(), stats.size()
                ));
            }
        }
        return new MatchSetup(
                setup.setupId(), setup.matchId(), width, height, rules, setup.teams(),
                concat(placedUnits, setup.units()), setup.headquarters(),
                concat(placedResources, setup.resourceNodes()),
                concat(placedBuildings, setup.buildings())
        );
    }

    /** Picks a random free footprint touching the nearest ring around the headquarters that still has room. */
    private static Position randomCellNear(
            Position center, int size, int width, int height, Set<Position> occupied, Random random
    ) {
        int maxRing = Math.max(width, height);
        for (int distance = 1; distance <= maxRing; distance++) {
            int ring = distance;
            List<Position> candidates = new ArrayList<>();
            for (int y = center.y() - ring - size + 1; y <= center.y() + ring; y++) {
                for (int x = center.x() - ring - size + 1; x <= center.x() + ring; x++) {
                    Position origin = new Position(x, y);
                    List<Position> footprint = cells(origin, size);
                    boolean onRing = footprint.stream().anyMatch(cell -> ringOf(center, cell) == ring)
                            && footprint.stream().allMatch(cell -> ringOf(center, cell) <= ring);
                    boolean free = footprint.stream().allMatch(cell -> cell.x() >= 0 && cell.y() >= 0
                            && cell.x() < width && cell.y() < height && !occupied.contains(cell));
                    if (onRing && free) {
                        candidates.add(origin);
                    }
                }
            }
            if (!candidates.isEmpty()) {
                return candidates.get(random.nextInt(candidates.size()));
            }
        }
        return null;
    }

    private static int ringOf(Position center, Position cell) {
        return Math.max(Math.abs(cell.x() - center.x()), Math.abs(cell.y() - center.y()));
    }

    private static List<Position> cells(Position origin, int size) {
        List<Position> result = new ArrayList<>();
        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                result.add(new Position(origin.x() + dx, origin.y() + dy));
            }
        }
        return result;
    }

    private static void claim(Position position, Team team, int width, int height, Set<Position> occupied) {
        if (position.x() < 0 || position.y() < 0 || position.x() >= width || position.y() >= height) {
            throw new IllegalArgumentException("Starter pack of team " + team.id() + " falls outside the map at "
                    + position);
        }
        if (!occupied.add(position)) {
            throw new IllegalArgumentException("Starter pack of team " + team.id() + " overlaps another object at "
                    + position);
        }
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

    /** Leave dx and dy empty to place the unit on a random free cell next to the headquarters. */
    public record PackUnit(UnitType type, Integer dx, Integer dy) {
    }
}
