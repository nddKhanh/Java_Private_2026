package com.antcolonywars.bot;

import com.antcolonywars.client.BatchReceipt;

public interface BatchGameBot extends GameBot {
    /** Sends the collected actions to the queues of their bots; they run one per bot per tick. */
    BatchReceipt submitBatch();

    void discardBatch();
}
