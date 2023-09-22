package com.ahmet.networking;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class WebServer {

    private final Logger logger;
    private static final String STATUS_ENDPOINT = "/status";
    private static final String HOME_PAGE_ENDPOINT = "/";
    private static final String HTML_PAGE = "/index.html";
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

        httpServer.createContext(STATUS_ENDPOINT, this::handleStatusCheckRequest);
        httpServer.createContext(onRequestCallback.getEndpoint(), this::handleTaskRequest);
        httpServer.createContext(HOME_PAGE_ENDPOINT, this::handleHomePageRequest);

        httpServer.setExecutor(Executors.newFixedThreadPool(8));
        httpServer.start();
        logger.warn("Server started listening on port {}", port);
    }

    private void handleHomePageRequest(HttpExchange exchange) throws IOException {
        if (!"get".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.close();
            return;
        }

        logger.warn("Received a homepage request");
        exchange.getResponseHeaders().add("Content-Type", "text/html");
        exchange.getResponseHeaders().add("Cache-Control", "no-cache");

        sendResponse(loadHtml(), exchange);
    }

    private byte[] loadHtml() {
        try (InputStream htmlResource = getClass().getResourceAsStream(WebServer.HTML_PAGE)) {
            if (htmlResource == null) {
                return new byte[0];
            }
            Document htmlDoc = Jsoup.parse(htmlResource, "UTF-8", "");

            String html = modifyHtml(htmlDoc);
            return html.getBytes();
        } catch (IOException e) {
            logger.error("HTML page could not be processed", e);
            return new byte[0];
        }

    }

    private String modifyHtml(Document htmlDoc) {
        Element element = htmlDoc.selectFirst("#server_name");
        if (element != null) {
            element.appendText(String.valueOf(port));
        }
        return htmlDoc.toString();
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
