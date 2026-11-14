package com.antcolonywars.core;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

public record Projectile(
        String id,
        String teamId,
        String sourceUnitId,
        String targetId,
        Position position,
        Position targetPosition,
        List<Position> path,
        int pathIndex,
        int damage
) {
    public Projectile {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(sourceUnitId, "sourceUnitId");
        Objects.requireNonNull(targetId, "targetId");
        Objects.requireNonNull(position, "position");
        Objects.requireNonNull(targetPosition, "targetPosition");
        boolean generatedPath = path == null || path.isEmpty();
        path = generatedPath ? bresenham(position, targetPosition) : List.copyOf(path);
        if (generatedPath && pathIndex == 0) {
            pathIndex = 1;
        }
        if (pathIndex < 1 || pathIndex >= path.size()) {
            throw new IllegalArgumentException("Projectile path index is invalid");
        }
        if (damage <= 0) {
            throw new IllegalArgumentException("Projectile damage must be positive");
        }
    }

    public Projectile(
            String id,
            String teamId,
            String sourceUnitId,
            String targetId,
            Position position,
            Position targetPosition,
            int damage
    ) {
        this(id, teamId, sourceUnitId, targetId, position, targetPosition, null, 1, damage);
    }

    public Position nextPosition() {
        return path.get(pathIndex);
    }

    public boolean reachesDestinationNext() {
        return pathIndex == path.size() - 1;
    }

    public Projectile moveNext() {
        return new Projectile(
                id, teamId, sourceUnitId, targetId, nextPosition(), targetPosition, path, pathIndex + 1, damage
        );
    }

    private static List<Position> bresenham(Position start, Position target) {
        List<Position> result = new ArrayList<>();
        int x = start.x();
        int y = start.y();
        int dx = Math.abs(target.x() - x);
        int dy = Math.abs(target.y() - y);
        int sx = Integer.signum(target.x() - x);
        int sy = Integer.signum(target.y() - y);
        int error = dx - dy;
        while (true) {
            result.add(new Position(x, y));
            if (x == target.x() && y == target.y()) {
                return result;
            }
            int doubled = error * 2;
            if (doubled > -dy) {
                error -= dy;
                x += sx;
            }
            if (doubled < dx) {
                error += dx;
                y += sy;
            }
        }
    }
}
