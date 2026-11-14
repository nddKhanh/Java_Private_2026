package com.antcolonywars.client;

record CommandResult(
        String commandId,
        boolean ok,
        String error,
        String errorMessage,
        Integer failedIndex,
        int appliedCount,
        long revision,
        long sequence
) {
}
