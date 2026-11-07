package com.antcolonywars.desktop;

public record CompilationMessage(Kind kind, long line, long column, String message) {
    public enum Kind {
        ERROR,
        WARNING,
        NOTE,
        OTHER
    }
}
