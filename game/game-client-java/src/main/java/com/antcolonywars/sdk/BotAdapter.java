package com.antcolonywars.sdk;

import com.antcolonywars.bot.GameBot;
import com.antcolonywars.client.BuildingType;
import com.antcolonywars.client.Direction;
import com.antcolonywars.client.GameState;
import com.antcolonywars.core.UnitType;

import java.util.Locale;
import java.util.Objects;

public final class BotAdapter implements Bot {
    private final GameBot delegate;

    public BotAdapter(GameBot delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void select(String id) {
        delegate.selectUnit(requireText(id, "id"));
    }

    @Override
    public void go(int direction) {
        delegate.move(direction(direction));
    }

    @Override
    public void hit(int direction) {
        delegate.attack(direction(direction));
    }

    @Override
    public void mine() {
        delegate.harvest();
    }

    @Override
    public void build(String id, String type, int direction) {
        delegate.build(
                requireText(id, "id"),
                BuildingType.valueOf(requireText(type, "type").toUpperCase(Locale.ROOT)),
                direction(direction)
        );
    }

    @Override
    public void spawn(String source, String type, String id, int direction) {
        requireText(source, "source");
        String unitId = requireText(id, "id");
        UnitType unitType = UnitType.valueOf(requireText(type, "type").toUpperCase(Locale.ROOT));
        delegate.spawn(unitId, unitId, unitType, direction(direction));
    }

    @Override
    public void idle() {
        delegate.waitTurn();
    }

    @Override
    public GameState state() {
        return delegate.getLatestState();
    }

    private static Direction direction(int value) {
        return switch (value) {
            case 1 -> Direction.UP;
            case 2 -> Direction.RIGHT;
            case 3 -> Direction.DOWN;
            case 4 -> Direction.LEFT;
            default -> throw new IllegalArgumentException("Hướng phải là 1, 2, 3 hoặc 4");
        };
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Thiếu " + name);
        }
        return value;
    }
}
