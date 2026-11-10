package com.antcolonywars.core;

public sealed interface ActionCommand extends GameCommand permits MoveCommand, WaitCommand, AttackCommand, SpawnCommand,
        HarvestCommand, BuildCommand, TickCommand {
    String actorTeamId();

    String unitId();
}
