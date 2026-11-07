package com.antcolonywars.core;

public sealed interface GameCommand permits ActionCommand, GetStateCommand {
    String commandId();
}
