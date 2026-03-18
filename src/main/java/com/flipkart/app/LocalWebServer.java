package com.flipkart.app;

import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * LocalWebServer — serves the Flipkart Clone webapp files over HTTP.
 */
public class LocalWebServer {

    private final String webappRoot;
    private final int    port;
    private HttpServer   server;

    private static final Map<String, String> MIME = new HashMap<>();
    static {
        MIME.put("html", "text/html; charset=utf-8");
        MIME.put("css",  "text/css");
        MIME.put("js",   "application/javascript");
        MIME.put("png",  "image/png");
        MIME.put("jpg",  "image/jpeg");
        MIME.put("jpeg", "image/jpeg");
        MIME.put("gif",  "image/gif");
        MIME.put("ico",  "image/x-icon");
        MIME.put("json", "application/json");
    }

    public LocalWebServer(String webappRoot, int port) {
        this.webappRoot = webappRoot;
        this.port       = port;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            String reqPath = exchange.getRequestURI().getPath();
            if (reqPath.equals("/")) reqPath = "/index.html";

            Path filePath = Paths.get(webappRoot + reqPath.replace("/", File.separator)).normalize();
            if (!filePath.startsWith(Paths.get(webappRoot).normalize())) {
                byte[] msg = "Forbidden".getBytes();
                exchange.sendResponseHeaders(403, msg.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(msg); }
                return;
            }

            if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                byte[] bytes = Files.readAllBytes(filePath);
                String name  = filePath.getFileName().toString();
                String ext   = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "html";
                exchange.getResponseHeaders().add("Content-Type", MIME.getOrDefault(ext, "application/octet-stream"));
                exchange.getResponseHeaders().add("Cache-Control", "no-cache");
                exchange.sendResponseHeaders(200, bytes.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
            } else {
                byte[] msg = ("404 Not found: " + reqPath).getBytes();
                exchange.sendResponseHeaders(404, msg.length);
                try (OutputStream os = exchange.getResponseBody()) { os.write(msg); }
            }
        });
        server.setExecutor(null);
        server.start();
    }

    public void stop() { if (server != null) server.stop(0); }
    public String getBaseUrl() { return "http://localhost:" + port; }
    public int    getPort()    { return port; }
}
