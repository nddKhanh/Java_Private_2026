package com.antcolonywars.demo;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.GameClient;
import com.antcolonywars.client.GameState;
import com.antcolonywars.core.Headquarters;
import com.antcolonywars.core.Position;
import com.antcolonywars.core.Unit;

import java.util.Comparator;
import java.util.List;

/** Blue's worker walks to the right edge and destroys every SWARM headquarters, clearing units in its way. */
public final class WinningScenario {
    private static final String WORKER = "worker-blue";
    private static final String ENEMY_FACTION = "SWARM";

    private WinningScenario() {
    }

    public static void main(String[] args) {
        try (GameClient bot = GameClient.connect(DemoConfig.serverUrl(), DemoConfig.blueToken())) {
            Position start = DemoConfig.position(bot.getLatestState(), WORKER);
            int edge = bot.getLatestState().snapshot().mapWidth() - 1;
            walk(bot, Direction.DOWN, 6 - start.y());
            walk(bot, Direction.RIGHT, edge - start.x());

            List<Headquarters> targets = enemyHeadquarters(bot.getLatestState());
            GameState state = bot.getLatestState();
            for (Headquarters target : targets) {
                Position here = DemoConfig.position(bot.getLatestState(), WORKER);
                Direction direction = target.position().y() < here.y() ? Direction.UP : Direction.DOWN;
                int approachY = target.position().y() + (direction == Direction.UP ? 1 : -1);
                walk(bot, direction, Math.abs(approachY - here.y()));
                state = destroy(bot, target.id(), direction);
            }
            System.out.printf(
                    "Match status: %s, winner: %s, revision: %d.%n",
                    state.status(),
                    state.winnerTeamId(),
                    state.revision()
            );
        }
    }

    /** Enemy headquarters on the right edge, the top one first, then the rest from top to bottom. */
    private static List<Headquarters> enemyHeadquarters(GameState state) {
        return state.snapshot().headquarters().stream()
                .filter(hq -> isEnemy(state, hq.teamId()))
                .sorted(Comparator.comparingInt(hq -> hq.position().y()))
                .toList();
    }

    /** Moves step by step; an enemy unit on the next cell is shot down first. */
    private static void walk(GameClient bot, Direction direction, int steps) {
        for (int step = 0; step < steps; step++) {
            while (blockingEnemy(bot.getLatestState(), direction) != null) {
                bot.attack(direction);
                bot.awaitSimulationIdle();
            }
            bot.move(direction);
        }
    }

    /** Attacks until the headquarters falls; enemy units standing in between are hit first. */
    private static GameState destroy(GameClient bot, String headquartersId, Direction direction) {
        GameState state = bot.getLatestState();
        while (headquarters(state, headquartersId).hp() > 0) {
            bot.attack(direction);
            state = bot.awaitSimulationIdle();
        }
        return state;
    }

    private static Unit blockingEnemy(GameState state, Direction direction) {
        Position here = DemoConfig.position(state, WORKER);
        Position next = switch (direction) {
            case UP -> new Position(here.x(), here.y() - 1);
            case DOWN -> new Position(here.x(), here.y() + 1);
            case LEFT -> new Position(here.x() - 1, here.y());
            case RIGHT -> new Position(here.x() + 1, here.y());
        };
        return state.snapshot().units().stream()
                .filter(unit -> unit.hp() > 0 && unit.position().equals(next) && isEnemy(state, unit.teamId()))
                .findFirst()
                .orElse(null);
    }

    private static Headquarters headquarters(GameState state, String id) {
        return state.snapshot().headquarters().stream()
                .filter(hq -> hq.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static boolean isEnemy(GameState state, String teamId) {
        return state.snapshot().teams().stream()
                .anyMatch(team -> team.id().equals(teamId) && team.factionId().equals(ENEMY_FACTION));
    }
}
