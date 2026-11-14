package com.antcolonywars.bot;

import com.antcolonywars.client.GameState;

public interface BatchGameBot extends GameBot {
    GameState submitBatch();

    void discardBatch();
}
