package com.antcolonywars.core;

import java.util.Objects;

/** A queued command that failed when its turn came; droppedCount counts the later commands of its batch removed with it. */
public record CommandRejection(
        String teamId,
        String batchId,
        String commandId,
        String subjectId,
        String action,
        ErrorCode error,
        int droppedCount
) {
    public CommandRejection {
        Objects.requireNonNull(teamId, "teamId");
        Objects.requireNonNull(commandId, "commandId");
        Objects.requireNonNull(error, "error");
    }

    public static String actionOf(ActionCommand command) {
        return switch (command) {
            case MoveCommand ignored -> "MOVE";
            case WaitCommand ignored -> "WAIT";
            case AttackCommand ignored -> "ATTACK";
            case SpawnCommand ignored -> "SPAWN";
            case HarvestCommand ignored -> "HARVEST";
            case BuildCommand ignored -> "BUILD";
            case TickCommand ignored -> "TICK";
            case InvalidActionCommand ignored -> "INVALID";
        };
    }
}
