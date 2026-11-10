package com.antcolonywars.bot;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.client.GameClient;
import com.antcolonywars.client.GameState;

import java.time.Duration;

public final class GameBots {
    private GameBots() {
    }

    public static GameBot connect(String serverUrl, String teamToken) {
        return new ClientGameBot(GameClient.connect(serverUrl, teamToken));
    }

    public static GameBot connect(String serverUrl, String teamToken, Duration timeout) {
        return new ClientGameBot(GameClient.connect(serverUrl, teamToken, timeout));
    }

    private static final class ClientGameBot implements GameBot {
        private final GameClient client;
        private String selectedUnitId;

        private ClientGameBot(GameClient client) {
            this.client = client;
        }

        @Override
        public GameBot selectUnit(String unitId) {
            if (unitId == null || unitId.isBlank()) {
                throw new IllegalArgumentException("unitId must not be blank");
            }
            selectedUnitId = unitId;
            return this;
        }

        @Override
        public String selectedUnitId() {
            return selectedUnitId;
        }

        @Override
        public GameState spawn(String unitId, String displayName, Direction direction) {
            return client.spawn(unitId, displayName, direction);
        }

        @Override
        public GameState harvest() {
            return selectedUnitId == null ? client.harvest() : client.harvest(selectedUnitId);
        }

        @Override
        public GameState build(String buildingId, BuildingType buildingType, Direction direction) {
            if (selectedUnitId == null) {
                return client.build(buildingId, buildingType, direction);
            }
            return client.build(selectedUnitId, buildingId, buildingType, direction);
        }

        @Override
        public GameState move(Direction direction) {
            return selectedUnitId == null
                    ? client.move(direction)
                    : client.move(selectedUnitId, direction);
        }

        @Override
        public GameState waitTurn() {
            return selectedUnitId == null
                    ? client.waitTurn()
                    : client.waitTurn(selectedUnitId);
        }

        @Override
        public GameState attack(String targetId) {
            return selectedUnitId == null
                    ? client.attack(targetId)
                    : client.attack(selectedUnitId, targetId);
        }

        @Override
        public GameState awaitSimulationIdle() {
            return client.awaitSimulationIdle();
        }

        @Override
        public GameState getLatestState() {
            return client.getLatestState();
        }

        @Override
        public void close() {
            client.close();
        }
    }
}
