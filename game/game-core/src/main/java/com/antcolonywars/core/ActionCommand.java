package com.antcolonywars.core;

public sealed interface ActionCommand extends GameCommand permits MoveCommand, WaitCommand, AttackCommand, SpawnCommand,
        HarvestCommand, BuildCommand {
    String actorTeamId();

    String unitId();
}
