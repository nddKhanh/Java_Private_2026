package com.antcolonywars.core;

import java.util.ArrayList;
import java.util.List;

public final class GameEngine {
    public ApplyResult apply(MatchState state, GameCommand command, RuleSet rules) {
        if (command instanceof GetStateCommand) {
            return ApplyResult.success(state, List.of());
        }

        ActionCommand action = (ActionCommand) command;
        if (state.status() == MatchStatus.FINISHED) {
            return ApplyResult.failure(state, ErrorCode.MATCH_FINISHED);
        }
        if (state.processedCommandIds().contains(action.commandId())) {
            return ApplyResult.failure(state, ErrorCode.DUPLICATE_COMMAND);
        }
        if (action instanceof TickCommand tick) {
            return applyTick(state, tick);
        }
        if (action instanceof SpawnCommand spawn) {
            return applySpawn(state, spawn, rules);
        }

        Unit actor = findUnit(state, action.unitId());
        if (actor == null || actor.hp() == 0) {
            return ApplyResult.failure(state, ErrorCode.UNIT_NOT_FOUND);
        }
        if (!actor.teamId().equals(action.actorTeamId())) {
            return ApplyResult.failure(state, ErrorCode.NOT_OWNER);
        }
        if (action instanceof HarvestCommand harvest) {
            return applyHarvest(state, actor, harvest, rules);
        }
        if (action instanceof BuildCommand build) {
            return applyBuild(state, actor, build, rules);
        }
        if (action instanceof MoveCommand move) {
            return applyMove(state, actor, move);
        }
        if (action instanceof WaitCommand wait) {
            return accept(state, state.units(), state.retiredUnitIds(), state.headquarters(), state.status(),
                    state.resourceNodes(), state.buildings(), state.teams(), state.projectiles(),
                    state.winnerTeamId(), wait, null);
        }
        return applyAttack(state, actor, (AttackCommand) action, rules);
    }

    private ApplyResult applySpawn(MatchState state, SpawnCommand command, RuleSet rules) {
        if (command.unitId().isBlank() || entityIdExists(state, command.unitId())
                || state.retiredUnitIds().contains(command.unitId())) {
            return ApplyResult.failure(state, ErrorCode.ENTITY_ID_TAKEN);
        }
        String displayName = command.displayName().trim();
        if (displayName.isEmpty() || displayName.length() > 32) {
            return ApplyResult.failure(state, ErrorCode.INVALID_DISPLAY_NAME);
        }
        try {
            UnitType.valueOf(command.unitType());
        } catch (IllegalArgumentException exception) {
            return ApplyResult.failure(state, ErrorCode.INVALID_UNIT_TYPE);
        }
        long teamUnitCount = state.units().stream()
                .filter(unit -> unit.teamId().equals(command.actorTeamId()))
                .count();
        if (teamUnitCount >= rules.maxUnitsPerTeam()) {
            return ApplyResult.failure(state, ErrorCode.UNIT_LIMIT_REACHED);
        }
        Headquarters headquarters = state.headquarters().stream()
                .filter(item -> item.teamId().equals(command.actorTeamId()) && item.hp() > 0)
                .findFirst()
                .orElse(null);
        if (headquarters == null) {
            return ApplyResult.failure(state, ErrorCode.HEADQUARTERS_NOT_FOUND);
        }
        int footprint = UnitType.SIEGE.name().equals(command.unitType()) ? 2 : 1;
        Position destination = adjacentPosition(
                headquarters.position(), 1, 1, footprint, footprint, command.direction()
        );
        List<Position> destinationCells = cells(destination, footprint, footprint);
        if (!withinBounds(state, destinationCells)) {
            return ApplyResult.failure(state, ErrorCode.OUT_OF_BOUNDS);
        }
        if (isOccupied(state, destinationCells, null)) {
            return ApplyResult.failure(state, ErrorCode.CELL_OCCUPIED);
        }

        List<Unit> units = new ArrayList<>(state.units());
        units.add(new Unit(
                command.unitId(), displayName, command.actorTeamId(), command.unitType(), destination,
                rules.spawnedUnitHp(), command.direction(), footprint, footprint
        ));
        return accept(state, units, state.retiredUnitIds(), state.headquarters(), state.status(),
                state.resourceNodes(), state.buildings(), state.teams(), state.projectiles(),
                state.winnerTeamId(), command, null);
    }

    private ApplyResult applyHarvest(MatchState state, Unit actor, HarvestCommand command, RuleSet rules) {
        ResourceNode resource = state.resourceNodes().stream()
                .filter(node -> distance(cells(actor), List.of(node.position())) <= rules.harvestRange())
                .filter(node -> node.amount() > 0)
                .findFirst()
                .orElse(null);
        if (resource == null) {
            boolean hasNodeInRange = state.resourceNodes().stream()
                    .anyMatch(node -> distance(cells(actor), List.of(node.position())) <= rules.harvestRange());
            return ApplyResult.failure(state, hasNodeInRange
                    ? ErrorCode.RESOURCE_DEPLETED
                    : ErrorCode.RESOURCE_NOT_FOUND);
        }
        int quantity = Math.min(rules.harvestAmount(), resource.amount());
        List<ResourceNode> resources = replaceResource(state.resourceNodes(), resource.harvest(quantity));
        Team team = findTeam(state, actor.teamId());
        List<Team> teams = replaceTeam(state.teams(), team.withResources(team.resources() + quantity));
        return accept(state, state.units(), state.retiredUnitIds(), state.headquarters(), state.status(),
                resources, state.buildings(), teams, state.projectiles(), state.winnerTeamId(), command, resource.id());
    }

    private ApplyResult applyBuild(MatchState state, Unit actor, BuildCommand command, RuleSet rules) {
        if (command.buildingId().isBlank() || entityIdExists(state, command.buildingId())) {
            return ApplyResult.failure(state, ErrorCode.BUILDING_ID_TAKEN);
        }
        int cost = buildingCost(command.buildingType(), rules);
        Team team = findTeam(state, actor.teamId());
        if (team.resources() < cost) {
            return ApplyResult.failure(state, ErrorCode.INSUFFICIENT_RESOURCES);
        }
        Position destination = adjacentPosition(
                actor.position(), actor.width(), actor.height(), 1, 1, command.direction()
        );
        List<Position> destinationCells = List.of(destination);
        if (!withinBounds(state, destinationCells) || isOccupied(state, destinationCells, actor.id())) {
            return ApplyResult.failure(state, ErrorCode.INVALID_BUILD_POSITION);
        }
        Building building = new Building(
                command.buildingId(), actor.teamId(), command.buildingType(), destination, rules.buildingHp()
        );
        List<Building> buildings = new ArrayList<>(state.buildings());
        buildings.add(building);
        List<Team> teams = replaceTeam(state.teams(), team.withResources(team.resources() - cost));
        return accept(state, state.units(), state.retiredUnitIds(), state.headquarters(), state.status(),
                state.resourceNodes(), buildings, teams, state.projectiles(), state.winnerTeamId(), command, building.id());
    }

    private ApplyResult applyMove(MatchState state, Unit actor, MoveCommand command) {
        Position destination = actor.position().move(command.direction());
        List<Position> destinationCells = cells(destination, actor.width(), actor.height());
        if (!withinBounds(state, destinationCells)) {
            return ApplyResult.failure(state, ErrorCode.OUT_OF_BOUNDS);
        }
        if (isOccupied(state, destinationCells, actor.id())) {
            return ApplyResult.failure(state, ErrorCode.CELL_OCCUPIED);
        }
        List<Unit> units = replaceUnit(state.units(), actor.moveTo(destination, command.direction()));
        return accept(state, units, state.retiredUnitIds(), state.headquarters(), state.status(),
                state.resourceNodes(), state.buildings(), state.teams(), state.projectiles(),
                state.winnerTeamId(), command, null);
    }

    private ApplyResult applyAttack(MatchState state, Unit actor, AttackCommand command, RuleSet rules) {
        Unit targetUnit = findUnit(state, command.targetId());
        Headquarters targetHeadquarters = findHeadquarters(state, command.targetId());
        if (targetUnit == null && targetHeadquarters == null) {
            return ApplyResult.failure(state, ErrorCode.TARGET_NOT_FOUND);
        }
        String targetTeamId = targetUnit != null ? targetUnit.teamId() : targetHeadquarters.teamId();
        List<Position> targetCells = targetUnit != null
                ? cells(targetUnit)
                : List.of(targetHeadquarters.position());
        int targetHp = targetUnit != null ? targetUnit.hp() : targetHeadquarters.hp();
        if (sameFaction(state, actor.teamId(), targetTeamId)) {
            return ApplyResult.failure(state, ErrorCode.FRIENDLY_TARGET);
        }
        if (targetHp == 0) {
            return ApplyResult.failure(state, ErrorCode.TARGET_DESTROYED);
        }
        if (distance(cells(actor), targetCells) > rules.attackRangeFor(actor.type())) {
            return ApplyResult.failure(state, ErrorCode.OUT_OF_RANGE);
        }

        List<Projectile> projectiles = new ArrayList<>(state.projectiles());
        Position targetCell = closestCell(targetCells, actor.position());
        projectiles.add(new Projectile(
                "projectile:" + command.commandId(), actor.teamId(), actor.id(), command.targetId(),
                closestCell(cells(actor), targetCell), rules.attackDamageFor(actor.type())
        ));
        return accept(state, state.units(), state.retiredUnitIds(), state.headquarters(), state.status(),
                state.resourceNodes(), state.buildings(), state.teams(), projectiles,
                state.winnerTeamId(), command, command.targetId());
    }

    private ApplyResult applyTick(MatchState state, TickCommand command) {
        List<Unit> units = state.units();
        List<String> retiredUnitIds = state.retiredUnitIds();
        List<Headquarters> headquarters = state.headquarters();
        List<Projectile> remaining = new ArrayList<>();
        MatchStatus status = state.status();
        String winnerTeamId = state.winnerTeamId();

        for (Projectile projectile : state.projectiles()) {
            Unit targetUnit = findUnit(units, projectile.targetId());
            Headquarters targetHeadquarters = findHeadquarters(headquarters, projectile.targetId());
            if (targetUnit == null && targetHeadquarters == null) {
                continue;
            }
            List<Position> targetCells = targetUnit != null
                    ? cells(targetUnit)
                    : List.of(targetHeadquarters.position());
            Position next = stepToward(projectile.position(), closestCell(targetCells, projectile.position()));
            if (!targetCells.contains(next)) {
                remaining.add(projectile.moveTo(next));
                continue;
            }
            if (targetUnit != null) {
                Unit damaged = targetUnit.takeDamage(projectile.damage());
                if (damaged.hp() == 0) {
                    units = removeUnit(units, damaged.id());
                    retiredUnitIds = new ArrayList<>(retiredUnitIds);
                    retiredUnitIds.add(damaged.id());
                } else {
                    units = replaceUnit(units, damaged);
                }
            } else {
                Headquarters damaged = targetHeadquarters.takeDamage(projectile.damage());
                headquarters = replaceHeadquarters(headquarters, damaged);
                if (damaged.hp() == 0 && hasSingleActiveFaction(state.teams(), headquarters)) {
                    status = MatchStatus.FINISHED;
                    winnerTeamId = projectile.teamId();
                    remaining.clear();
                    break;
                }
            }
        }

        return accept(state, units, retiredUnitIds, headquarters, status, state.resourceNodes(), state.buildings(),
                state.teams(), remaining, winnerTeamId, command, null);
    }

    private ApplyResult accept(
            MatchState previous,
            List<Unit> units,
            List<String> retiredUnitIds,
            List<Headquarters> headquarters,
            MatchStatus status,
            List<ResourceNode> resourceNodes,
            List<Building> buildings,
            List<Team> teams,
            List<Projectile> projectiles,
            String winnerTeamId,
            ActionCommand command,
            String targetId
    ) {
        long revision = previous.revision() + 1;
        long sequence = previous.eventSequence() + 1;
        List<String> processedCommandIds = new ArrayList<>(previous.processedCommandIds());
        processedCommandIds.add(command.commandId());
        MatchState next = new MatchState(
                previous.matchId(), previous.setupId(), previous.rulesVersion(), status, winnerTeamId,
                revision, sequence, previous.simulationTick() + (command instanceof TickCommand ? 1 : 0),
                previous.mapWidth(), previous.mapHeight(), teams, units, retiredUnitIds,
                headquarters, resourceNodes, buildings, projectiles, processedCommandIds
        );
        GameEvent event = new GameEvent(
                sequence, revision, command.commandId(), actionName(command), command.actorTeamId(),
                command.unitId(), targetId
        );
        return ApplyResult.success(next, List.of(event));
    }

    private String actionName(ActionCommand command) {
        if (command instanceof MoveCommand) return "MOVE";
        if (command instanceof WaitCommand) return "WAIT";
        if (command instanceof AttackCommand) return "ATTACK";
        if (command instanceof SpawnCommand) return "SPAWN";
        if (command instanceof HarvestCommand) return "HARVEST";
        if (command instanceof TickCommand) return "TICK";
        return "BUILD";
    }

    private boolean withinBounds(MatchState state, List<Position> positions) {
        return positions.stream().allMatch(position -> position.x() >= 0 && position.x() < state.mapWidth()
                && position.y() >= 0 && position.y() < state.mapHeight());
    }

    private boolean isOccupied(MatchState state, List<Position> positions, String ignoredUnitId) {
        return state.units().stream()
                .filter(unit -> unit.hp() > 0 && !unit.id().equals(ignoredUnitId))
                .flatMap(unit -> cells(unit).stream())
                .anyMatch(positions::contains)
                || state.headquarters().stream()
                .filter(headquarters -> headquarters.hp() > 0)
                .map(Headquarters::position)
                .anyMatch(positions::contains)
                || state.buildings().stream()
                .filter(building -> building.hp() > 0)
                .map(Building::position)
                .anyMatch(positions::contains);
    }

    private Unit findUnit(MatchState state, String unitId) {
        return findUnit(state.units(), unitId);
    }

    private Unit findUnit(List<Unit> units, String unitId) {
        return units.stream().filter(unit -> unit.id().equals(unitId)).findFirst().orElse(null);
    }

    private Headquarters findHeadquarters(MatchState state, String headquartersId) {
        return findHeadquarters(state.headquarters(), headquartersId);
    }

    private Headquarters findHeadquarters(List<Headquarters> headquarters, String headquartersId) {
        return headquarters.stream()
                .filter(item -> item.id().equals(headquartersId))
                .findFirst().orElse(null);
    }

    private List<Position> cells(Unit unit) {
        return cells(unit.position(), unit.width(), unit.height());
    }

    private List<Position> cells(Position anchor, int width, int height) {
        List<Position> cells = new ArrayList<>(width * height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cells.add(new Position(anchor.x() + x, anchor.y() + y));
            }
        }
        return cells;
    }

    private int distance(List<Position> first, List<Position> second) {
        return first.stream()
                .flatMapToInt(left -> second.stream().mapToInt(left::manhattanDistance))
                .min()
                .orElseThrow();
    }

    private Position closestCell(List<Position> candidates, Position origin) {
        return candidates.stream()
                .min(java.util.Comparator
                        .comparingInt(origin::manhattanDistance)
                        .thenComparingInt(Position::y)
                        .thenComparingInt(Position::x))
                .orElseThrow();
    }

    private Position stepToward(Position current, Position target) {
        if (current.x() != target.x()) {
            return new Position(current.x() + Integer.signum(target.x() - current.x()), current.y());
        }
        if (current.y() != target.y()) {
            return new Position(current.x(), current.y() + Integer.signum(target.y() - current.y()));
        }
        return current;
    }

    private Position adjacentPosition(
            Position source,
            int sourceWidth,
            int sourceHeight,
            int targetWidth,
            int targetHeight,
            Direction direction
    ) {
        return switch (direction) {
            case RIGHT -> new Position(source.x() + sourceWidth, source.y());
            case LEFT -> new Position(source.x() - targetWidth, source.y());
            case DOWN -> new Position(source.x(), source.y() + sourceHeight);
            case UP -> new Position(source.x(), source.y() - targetHeight);
        };
    }

    private boolean entityIdExists(MatchState state, String entityId) {
        return findUnit(state, entityId) != null || findHeadquarters(state, entityId) != null
                || state.resourceNodes().stream().anyMatch(node -> node.id().equals(entityId))
                || state.buildings().stream().anyMatch(building -> building.id().equals(entityId));
    }

    private Team findTeam(MatchState state, String teamId) {
        return state.teams().stream().filter(team -> team.id().equals(teamId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown team: " + teamId));
    }

    private boolean sameFaction(MatchState state, String firstTeamId, String secondTeamId) {
        return findTeam(state, firstTeamId).factionId().equals(findTeam(state, secondTeamId).factionId());
    }

    private boolean hasSingleActiveFaction(List<Team> teams, List<Headquarters> headquarters) {
        return headquarters.stream()
                .filter(headquartersItem -> headquartersItem.hp() > 0)
                .map(headquartersItem -> teams.stream()
                        .filter(team -> team.id().equals(headquartersItem.teamId()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Unknown team: " + headquartersItem.teamId()
                        ))
                        .factionId())
                .distinct()
                .limit(2)
                .count() == 1;
    }

    private int buildingCost(BuildingType type, RuleSet rules) {
        return switch (type) {
            case BARRACKS -> rules.barracksCost();
            case TOWER -> rules.towerCost();
            case WALL -> rules.wallCost();
        };
    }

    private List<Unit> replaceUnit(List<Unit> units, Unit replacement) {
        return units.stream().map(unit -> unit.id().equals(replacement.id()) ? replacement : unit).toList();
    }

    private List<Unit> removeUnit(List<Unit> units, String unitId) {
        return units.stream().filter(unit -> !unit.id().equals(unitId)).toList();
    }

    private List<Headquarters> replaceHeadquarters(List<Headquarters> headquarters, Headquarters replacement) {
        return headquarters.stream().map(item -> item.id().equals(replacement.id()) ? replacement : item).toList();
    }

    private List<ResourceNode> replaceResource(List<ResourceNode> resources, ResourceNode replacement) {
        return resources.stream().map(resource -> resource.id().equals(replacement.id()) ? replacement : resource).toList();
    }

    private List<Team> replaceTeam(List<Team> teams, Team replacement) {
        return teams.stream().map(team -> team.id().equals(replacement.id()) ? replacement : team).toList();
    }
}
