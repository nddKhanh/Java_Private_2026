package com.antcolonywars.sdk;

import com.antcolonywars.client.GameState;

public interface Bot {
    void select(String id);

    void go(int direction);

    void hit(int direction);

    void mine();

    void build(String id, String type, int direction);

    void spawn(String source, String type, String id, int direction);

    void idle();

    GameState state();
}
