package com.antcolonywars.core;

public record Position(int x, int y) {
    public Position move(Direction direction) {
        return new Position(x + direction.dx(), y + direction.dy());
    }

    public int manhattanDistance(Position other) {
        return Math.abs(x - other.x) + Math.abs(y - other.y);
    }
}
