package com.antcolonywars.bot;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.client.BatchAction;
import com.antcolonywars.client.GameClient;
import com.antcolonywars.client.GameState;
import com.antcolonywars.core.UnitType;
import com.antcolonywars.core.BatchMode;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    public static GameBot connectBatch(
            String serverUrl,
            String accessToken,
            String matchId,
            Duration timeout,
            BatchMode mode
    ) {
        return new BatchingGameBot(GameClient.connect(serverUrl, accessToken, matchId, timeout), mode);
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

    private static final class BatchingGameBot implements GameBot {
        private final GameClient client;
        private final BatchMode mode;
        private final List<BatchAction> actions = new ArrayList<>();
        private final GameState batchSnapshot;
        private String selectedUnitId;

        private BatchingGameBot(GameClient client, BatchMode mode) {
            this.client = client;
            this.mode = mode;
            this.batchSnapshot = client.getLatestState();
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
            return spawn(unitId, displayName, UnitType.WORKER, direction);
        }

        @Override
        public GameState spawn(String unitId, String displayName, UnitType unitType, Direction direction) {
            actions.add(BatchAction.spawn(unitId, displayName, unitType.name(), direction));
            return batchSnapshot;
        }

        @Override
        public GameState harvest() {
            actions.add(BatchAction.harvest(selectedUnitId));
            return batchSnapshot;
        }

        @Override
        public GameState build(String buildingId, BuildingType buildingType, Direction direction) {
            actions.add(BatchAction.build(selectedUnitId, buildingId, buildingType, direction));
            return batchSnapshot;
        }

        @Override
        public GameState move(Direction direction) {
            actions.add(BatchAction.move(selectedUnitId, direction));
            return batchSnapshot;
        }

        @Override
        public GameState waitTurn() {
            actions.add(BatchAction.waitTurn(selectedUnitId));
            return batchSnapshot;
        }

        @Override
        public GameState attack(Direction direction) {
            actions.add(BatchAction.attack(selectedUnitId, direction));
            return batchSnapshot;
        }

        @Override
        public GameState attack(String unitId, Direction direction) {
            actions.add(BatchAction.attack(unitId, direction));
            return batchSnapshot;
        }

        @Override
        public GameState awaitSimulationIdle() {
            submit();
            return client.awaitSimulationIdle();
        }

        @Override
        public GameState getLatestState() {
            return batchSnapshot;
        }

        @Override
        public void close() {
            try {
                submit();
            } finally {
                client.close();
            }
        }

        private void submit() {
            if (actions.isEmpty()) {
                return;
            }
            List<BatchAction> submitted = List.copyOf(actions);
            actions.clear();
            client.submitBatch(submitted, mode);
        }
    }
}
