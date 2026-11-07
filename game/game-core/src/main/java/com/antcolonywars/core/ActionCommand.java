package com.antcolonywars.core;

public sealed interface ActionCommand extends GameCommand permits MoveCommand, WaitCommand, AttackCommand, SpawnCommand {
    String actorTeamId();

    String unitId();
}
