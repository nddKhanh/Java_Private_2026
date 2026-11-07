package com.antcolonywars.client;

record CommandResult(String commandId, boolean ok, String error, long revision, long sequence) {
}
