package com.antcolonywars.client;

import java.util.List;

/** Server acknowledgement of a batch: commands added to bot queues and commands refused on arrival. */
public record BatchReceipt(String batchId, List<BatchAction> actions, int queuedCount, List<QueueRejection> rejections) {
    public BatchReceipt {
        actions = List.copyOf(actions);
        rejections = List.copyOf(rejections);
    }

    /** Console text for a refused command: position, bot, action and reason. */
    public String describe(QueueRejection rejection) {
        return GameClient.describe(rejection, actions);
    }
}
