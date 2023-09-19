package com.ahmet.networking;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {

    private final Logger logger;
    private static final String STATUS_ENDPOINT = "/status";
    private final int port;
    private final OnRequestCallback onRequestCallback;
    private HttpServer httpServer;

    public WebServer(int port, OnRequestCallback onRequestCallback) {
        this.port = port;
        this.onRequestCallback = onRequestCallback;
        this.logger = LoggerFactory.getLogger(WebServer.class);
    }

    public void startServer() throws IOException {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        } catch (IOException e) {
            logger.error("Failed to start server. Exiting the application...", e);
            throw e;
        }

        HttpContext statusContext = httpServer.createContext(STATUS_ENDPOINT);
        statusContext.setHandler(this::handleStatusCheckRequest);

        HttpContext taskContext = httpServer.createContext(onRequestCallback.getEndpoint());
        taskContext.setHandler(this::handleTaskRequest);

        httpServer.setExecutor(Executors.newFixedThreadPool(8));
        httpServer.start();
        logger.warn("Server started listening on port {}", port);
    }

    private void handleStatusCheckRequest(HttpExchange exchange) throws IOException {
        if (!"get".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.close();
            return;
        }
        String responseMsg = "Server is alive\n";
        sendResponse(responseMsg.getBytes(), exchange);
    }

    private void handleTaskRequest(HttpExchange exchange) throws IOException {
        if (!"post".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.close();
            return;
        }

        byte[] responseBytes = onRequestCallback.handleRequest(exchange.getRequestBody().readAllBytes());
        sendResponse(responseBytes, exchange);
    }

    private void sendResponse(byte[] responseBytes, HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(200, responseBytes.length);

        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(responseBytes);
        outputStream.flush();
        outputStream.close();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
