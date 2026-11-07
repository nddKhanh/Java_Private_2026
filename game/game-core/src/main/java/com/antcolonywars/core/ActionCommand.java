package com.antcolonywars.core;

public sealed interface ActionCommand extends GameCommand permits MoveCommand, WaitCommand, AttackCommand {
    String actorTeamId();

    String unitId();
}
