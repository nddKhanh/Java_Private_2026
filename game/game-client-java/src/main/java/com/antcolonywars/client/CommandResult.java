package com.antcolonywars.client;

import java.util.List;

record CommandResult(
        String commandId,
        boolean ok,
        String error,
        String errorMessage,
        int queuedCount,
        List<QueueRejection> rejections,
        long revision,
        long sequence
) {
}
