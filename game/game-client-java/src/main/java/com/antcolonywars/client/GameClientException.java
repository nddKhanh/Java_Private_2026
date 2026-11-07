package com.antcolonywars.client;

public class GameClientException extends RuntimeException {
    private final String code;

    public GameClientException(String code, String message) {
        super(message);
        this.code = code;
    }

    public GameClientException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
