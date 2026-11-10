package com.antcolonywars.server;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public final class ApiExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handle(ApiException exception) {
        return ResponseEntity.status(exception.status()).body(Map.of(
                "code", exception.code(),
                "message", exception.getMessage()
        ));
    }
}
