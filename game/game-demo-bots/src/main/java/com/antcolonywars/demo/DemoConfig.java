package com.antcolonywars.demo;

final class DemoConfig {
    private DemoConfig() {
    }

    static String serverUrl() {
        return environmentOrDefault("GAME_WS_URL", "ws://localhost:8080/game");
    }

    static String blueToken() {
        return environmentOrDefault("GAME_BLUE_TOKEN", "blue-token");
    }

    static String redToken() {
        return environmentOrDefault("GAME_RED_TOKEN", "red-token");
    }

    private static String environmentOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
