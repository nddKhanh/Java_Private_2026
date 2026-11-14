package com.antcolonywars.sdk;

import com.antcolonywars.client.GameState;

public interface Bot {
    void select(String id);

    void go(int direction);

    void hit(int direction);

    void mine();

    void build(String id, String type, int direction);

    void spawn(String id, String type, int direction);

    void stay();

    GameState state();
}
