import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class Submit {
    private static final URI DESKTOP_ENDPOINT = URI.create("http://127.0.0.1:17321/submit");

    private Submit() {
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> environment = readEnvironment(Path.of(".env"));
        String key = required(environment, "MATCH_KEY");
        String mode = environment.getOrDefault("BATCH_MODE", "ATOMIC").trim().toUpperCase();
        String source = Files.readString(Path.of("src", "main", "java", "Main.java"));

        byte[] body = new ObjectMapper().writeValueAsBytes(Map.of(
                "className", "Main",
                "source", source,
                "pairingKey", key,
                "batchMode", mode
        ));
        HttpRequest request = HttpRequest.newBuilder(DESKTOP_ENDPOINT)
                .timeout(Duration.ofSeconds(5))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(
                request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
        );
        if (response.statusCode() != 202) {
            throw new IllegalStateException("Desktop từ chối source: " + response.body());
        }
        System.out.println("Đã gửi Main.java tới Desktop Code Studio.");
    }

    private static Map<String, String> readEnvironment(Path path) throws Exception {
        Map<String, String> values = new HashMap<>();
        for (String line : Files.readAllLines(path)) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int separator = trimmed.indexOf('=');
            if (separator > 0) {
                values.put(trimmed.substring(0, separator).trim(), trimmed.substring(separator + 1).trim());
            }
        }
        return values;
    }

    private static String required(Map<String, String> values, String name) {
        String value = values.get(name);
        if (value == null || value.isBlank() || "000000".equals(value)) {
            throw new IllegalArgumentException("Hãy điền " + name + " trong .env");
        }
        return value;
    }
}
