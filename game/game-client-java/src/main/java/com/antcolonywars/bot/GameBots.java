package com.antcolonywars.bot;

import com.antcolonywars.client.Direction;
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

    private record ClientGameBot(GameClient client) implements GameBot {
        @Override
        public GameState move(Direction direction) {
            return client.move(direction);
        }

        @Override
        public GameState waitTurn() {
            return client.waitTurn();
        }

        @Override
        public GameState attack(String targetId) {
            return client.attack(targetId);
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
