package com.antcolonywars.bot;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.client.GameClient;
import com.antcolonywars.client.GameState;
import com.antcolonywars.core.UnitType;

import java.time.Duration;

public final class GameBots {
    private GameBots() {
    }

    public static GameBot connect(String serverUrl, String accessToken) {
        return new ClientGameBot(GameClient.connect(serverUrl, accessToken));
    }

    public static GameBot connect(String serverUrl, String accessToken, Duration timeout) {
        return new ClientGameBot(GameClient.connect(serverUrl, accessToken, timeout));
    }

    public static GameBot connect(String serverUrl, String accessToken, String matchId) {
        return new ClientGameBot(GameClient.connect(serverUrl, accessToken, matchId));
    }

    public static GameBot connect(String serverUrl, String accessToken, String matchId, Duration timeout) {
        return new ClientGameBot(GameClient.connect(serverUrl, accessToken, matchId, timeout));
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
        public GameState spawn(String unitId, String displayName, UnitType unitType, Direction direction) {
            return client.spawn(unitId, displayName, unitType, direction);
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
        public GameState attack(Direction direction) {
            return selectedUnitId == null
                    ? client.attack(direction)
                    : client.attack(selectedUnitId, direction);
        }

        @Override
        public GameState attack(String unitId, Direction direction) {
            return client.attack(unitId, direction);
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
