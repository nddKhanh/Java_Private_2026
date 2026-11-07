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
        if (action instanceof MoveCommand move) {
            return applyMove(state, actor, move);
        }
        if (action instanceof WaitCommand wait) {
            return accept(state, state.units(), state.retiredUnitIds(), state.headquarters(), state.status(),
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
        Position destination = headquarters.position().move(command.direction());
        if (!withinBounds(state, destination)) {
            return ApplyResult.failure(state, ErrorCode.OUT_OF_BOUNDS);
        }
        if (isOccupied(state, destination)) {
            return ApplyResult.failure(state, ErrorCode.CELL_OCCUPIED);
        }

        List<Unit> units = new ArrayList<>(state.units());
        units.add(new Unit(
                command.unitId(),
                displayName,
                command.actorTeamId(),
                "WORKER",
                destination,
                rules.spawnedUnitHp(),
                command.direction()
        ));
        return accept(state, units, state.retiredUnitIds(), state.headquarters(), state.status(),
                state.winnerTeamId(), command, null);
    }

    private ApplyResult applyMove(MatchState state, Unit actor, MoveCommand command) {
        Position destination = actor.position().move(command.direction());
        if (!withinBounds(state, destination)) {
            return ApplyResult.failure(state, ErrorCode.OUT_OF_BOUNDS);
        }
        if (isOccupied(state, destination)) {
            return ApplyResult.failure(state, ErrorCode.CELL_OCCUPIED);
        }

        List<Unit> units = replaceUnit(state.units(), actor.moveTo(destination, command.direction()));
        return accept(state, units, state.retiredUnitIds(), state.headquarters(), state.status(),
                state.winnerTeamId(), command, null);
    }

    private ApplyResult applyAttack(MatchState state, Unit actor, AttackCommand command, RuleSet rules) {
        Unit targetUnit = findUnit(state, command.targetId());
        Headquarters targetHeadquarters = findHeadquarters(state, command.targetId());
        if (targetUnit == null && targetHeadquarters == null) {
            return ApplyResult.failure(state, ErrorCode.TARGET_NOT_FOUND);
        }

        String targetTeamId = targetUnit != null ? targetUnit.teamId() : targetHeadquarters.teamId();
        Position targetPosition = targetUnit != null ? targetUnit.position() : targetHeadquarters.position();
        int targetHp = targetUnit != null ? targetUnit.hp() : targetHeadquarters.hp();
        if (actor.teamId().equals(targetTeamId)) {
            return ApplyResult.failure(state, ErrorCode.FRIENDLY_TARGET);
        }
        if (targetHp == 0) {
            return ApplyResult.failure(state, ErrorCode.TARGET_DESTROYED);
        }
        if (actor.position().manhattanDistance(targetPosition) > rules.attackRange()) {
            return ApplyResult.failure(state, ErrorCode.OUT_OF_RANGE);
        }

        List<Unit> units = state.units();
        List<String> retiredUnitIds = state.retiredUnitIds();
        List<Headquarters> headquarters = state.headquarters();
        MatchStatus status = state.status();
        String winnerTeamId = state.winnerTeamId();
        if (targetUnit != null) {
            Unit damaged = targetUnit.takeDamage(rules.attackDamage());
            if (damaged.hp() == 0) {
                units = removeUnit(units, damaged.id());
                retiredUnitIds = new ArrayList<>(retiredUnitIds);
                retiredUnitIds.add(damaged.id());
            } else {
                units = replaceUnit(units, damaged);
            }
        } else {
            Headquarters damaged = targetHeadquarters.takeDamage(rules.attackDamage());
            headquarters = replaceHeadquarters(headquarters, damaged);
            if (damaged.hp() == 0) {
                status = MatchStatus.FINISHED;
                winnerTeamId = actor.teamId();
            }
        }

        return accept(state, units, retiredUnitIds, headquarters, status, winnerTeamId, command, command.targetId());
    }

    private ApplyResult accept(
            MatchState previous,
            List<Unit> units,
            List<String> retiredUnitIds,
            List<Headquarters> headquarters,
            MatchStatus status,
            String winnerTeamId,
            ActionCommand command,
            String targetId
    ) {
        long revision = previous.revision() + 1;
        long sequence = previous.eventSequence() + 1;
        List<String> processedCommandIds = new ArrayList<>(previous.processedCommandIds());
        processedCommandIds.add(command.commandId());
        MatchState next = new MatchState(
                previous.matchId(),
                previous.setupId(),
                previous.rulesVersion(),
                status,
                winnerTeamId,
                revision,
                sequence,
                previous.mapWidth(),
                previous.mapHeight(),
                previous.teams(),
                units,
                retiredUnitIds,
                headquarters,
                processedCommandIds
        );
        GameEvent event = new GameEvent(
                sequence,
                revision,
                command.commandId(),
                actionName(command),
                command.actorTeamId(),
                command.unitId(),
                targetId
        );
        return ApplyResult.success(next, List.of(event));
    }

    private String actionName(ActionCommand command) {
        if (command instanceof MoveCommand) {
            return "MOVE";
        }
        if (command instanceof WaitCommand) {
            return "WAIT";
        }
        if (command instanceof AttackCommand) {
            return "ATTACK";
        }
        return "SPAWN";
    }

    private boolean withinBounds(MatchState state, Position position) {
        return position.x() >= 0 && position.x() < state.mapWidth()
                && position.y() >= 0 && position.y() < state.mapHeight();
    }

    private boolean isOccupied(MatchState state, Position position) {
        return state.units().stream().anyMatch(unit -> unit.hp() > 0 && unit.position().equals(position))
                || state.headquarters().stream()
                .anyMatch(headquarters -> headquarters.hp() > 0 && headquarters.position().equals(position));
    }

    private Unit findUnit(MatchState state, String unitId) {
        return state.units().stream()
                .filter(unit -> unit.id().equals(unitId))
                .findFirst()
                .orElse(null);
    }

    private boolean entityIdExists(MatchState state, String entityId) {
        return findUnit(state, entityId) != null || findHeadquarters(state, entityId) != null;
    }

    private Headquarters findHeadquarters(MatchState state, String headquartersId) {
        return state.headquarters().stream()
                .filter(headquarters -> headquarters.id().equals(headquartersId))
                .findFirst()
                .orElse(null);
    }

    private List<Unit> replaceUnit(List<Unit> units, Unit replacement) {
        return units.stream()
                .map(unit -> unit.id().equals(replacement.id()) ? replacement : unit)
                .toList();
    }

    private List<Unit> removeUnit(List<Unit> units, String unitId) {
        return units.stream()
                .filter(unit -> !unit.id().equals(unitId))
                .toList();
    }

    private List<Headquarters> replaceHeadquarters(
            List<Headquarters> headquarters,
            Headquarters replacement
    ) {
        return headquarters.stream()
                .map(item -> item.id().equals(replacement.id()) ? replacement : item)
                .toList();
    }
}
