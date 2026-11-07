package com.antcolonywars.bot;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.GameState;

public interface GameBot extends AutoCloseable {
    GameState move(Direction direction);

    GameState waitTurn();

    GameState attack(String targetId);

    GameState getLatestState();

    @Override
    void close();
}
