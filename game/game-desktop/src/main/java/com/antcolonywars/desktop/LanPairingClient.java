package com.antcolonywars.desktop;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LanPairingClient {
    private static final int SERVER_PORT = 8080;
    private static final int MAX_CONCURRENT_REQUESTS = 48;
    private static final Duration REQUEST_TIMEOUT = Duration.ofMillis(700);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(350))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PairingConfiguration find(String pairingKey) {
        String key = pairingKey == null ? "" : pairingKey.trim().toUpperCase();
        if (!key.matches("[A-Z2-9]{6}")) {
            throw new IllegalArgumentException("Pairing key must contain 6 letters or digits");
        }

        List<String> hosts = candidateHosts();
        try (ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT_REQUESTS)) {
            CompletionService<Optional<PairingConfiguration>> requests = new ExecutorCompletionService<>(executor);
            for (String host : hosts) {
                requests.submit(() -> fetch(host, key));
            }
            for (int index = 0; index < hosts.size(); index++) {
                Optional<PairingConfiguration> result = requests.take().get();
                if (result.isPresent()) {
                    executor.shutdownNow();
                    return result.get();
                }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("LAN discovery was interrupted", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("LAN discovery failed", exception);
        }
        throw new IllegalStateException("No game server accepted this pairing key on the local network");
    }

    private Optional<PairingConfiguration> fetch(String host, String key) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + host + ":" + SERVER_PORT + "/api/desktop/pairing/" + key))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(response.body(), PairingConfiguration.class));
        } catch (IOException exception) {
            return Optional.empty();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    private List<String> candidateHosts() {
        Set<String> hosts = new LinkedHashSet<>();
        hosts.add("127.0.0.1");
        try {
            List<Inet4Address> addresses = NetworkInterface.networkInterfaces()
                    .filter(this::isUsable)
                    .flatMap(network -> network.getInterfaceAddresses().stream())
                    .map(InterfaceAddress::getAddress)
                    .filter(Inet4Address.class::isInstance)
                    .map(Inet4Address.class::cast)
                    .filter(address -> address.isSiteLocalAddress() && !address.isLoopbackAddress())
                    .sorted(Comparator.comparingInt(this::networkPriority))
                    .toList();
            for (Inet4Address address : addresses) {
                byte[] bytes = address.getAddress();
                for (int host = 1; host < 255; host++) {
                    hosts.add((bytes[0] & 255) + "." + (bytes[1] & 255) + "." + (bytes[2] & 255) + "." + host);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect local network interfaces", exception);
        }
        return new ArrayList<>(hosts);
    }

    private boolean isUsable(NetworkInterface network) {
        try {
            return network.isUp() && !network.isLoopback();
        } catch (IOException exception) {
            return false;
        }
    }

    private int networkPriority(Inet4Address address) {
        byte[] bytes = address.getAddress();
        int first = bytes[0] & 255;
        int second = bytes[1] & 255;
        if (first == 192 && second == 168) {
            return 0;
        }
        if (first == 10) {
            return 1;
        }
        return 2;
    }
}
