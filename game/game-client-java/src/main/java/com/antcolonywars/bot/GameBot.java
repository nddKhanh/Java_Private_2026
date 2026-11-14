package com.antcolonywars.bot;

import com.antcolonywars.client.Direction;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.client.GameState;
import com.antcolonywars.core.UnitType;

public interface GameBot extends AutoCloseable {
    GameBot selectUnit(String unitId);

    String selectedUnitId();

    GameState spawn(String unitId, String displayName, Direction direction);

    GameState spawn(String unitId, String displayName, UnitType unitType, Direction direction);

    GameState spawn(String sourceId, String unitId, String displayName, UnitType unitType, Direction direction);

    GameState harvest();

    GameState build(String buildingId, BuildingType buildingType, Direction direction);

    GameState move(Direction direction);

    GameState waitTurn();

    GameState attack(Direction direction);

    GameState attack(String unitId, Direction direction);

    GameState awaitSimulationIdle();

    GameState getLatestState();

    @Override
    void close();
}
