package com.antcolonywars.bot;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.client.GameState;

public interface GameBot extends AutoCloseable {
    GameBot selectUnit(String unitId);

    String selectedUnitId();

    GameState spawn(String unitId, String displayName, Direction direction);

    GameState harvest();

    GameState build(String buildingId, BuildingType buildingType, Direction direction);

    GameState move(Direction direction);

    GameState waitTurn();

    GameState attack(String targetId);

    GameState getLatestState();

    @Override
    void close();
}
