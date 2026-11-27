package com.antcolonywars.server;

import com.antcolonywars.core.ActionCommand;
import com.antcolonywars.core.AttackCommand;
import com.antcolonywars.core.BuildCommand;
import com.antcolonywars.core.CommandRejection;
import com.antcolonywars.core.MoveCommand;
import com.antcolonywars.core.SpawnCommand;

/** One pending command as shown in the bot's queue; target is the new unit or building id when there is one. */
public record QueuedCommandView(String commandId, String action, String direction, String type, String target) {
    public static QueuedCommandView of(ActionCommand command) {
        String action = CommandRejection.actionOf(command);
        return switch (command) {
            case MoveCommand move -> new QueuedCommandView(command.commandId(), action, move.direction().name(), null, null);
            case AttackCommand attack -> new QueuedCommandView(
                    command.commandId(), action, attack.direction().name(), null, null);
            case SpawnCommand spawn -> new QueuedCommandView(
                    command.commandId(), action, spawn.direction().name(), spawn.unitType(), spawn.unitId());
            case BuildCommand build -> new QueuedCommandView(
                    command.commandId(), action, build.direction().name(), build.buildingType().name(), build.buildingId());
            default -> new QueuedCommandView(command.commandId(), action, null, null, null);
        };
    }
}
