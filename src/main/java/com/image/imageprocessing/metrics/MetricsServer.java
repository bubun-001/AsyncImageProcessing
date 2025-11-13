package com.image.imageprocessing.metrics;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public final class MetricsServer {

    private static HttpServer server;

    private MetricsServer() {
    }

    public static synchronized void start(int port) {
        if (server != null) {
            return;
        }
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/metrics", exchange -> {
                String response = MetricsRegistry.registry().scrape();
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().add("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(bytes);
                }
            });
            server.setExecutor(null);
            server.start();
            System.out.println("Prometheus metrics server started on http://localhost:" + port + "/metrics");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to start metrics server", e);
        }
    }
}
