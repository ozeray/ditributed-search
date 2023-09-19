package com.ahmet;

import com.ahmet.management.OnElectionCallback;
import com.ahmet.management.ServiceRegistry;
import com.ahmet.networking.OnRequestCallback;
import com.ahmet.networking.WebServer;
import com.ahmet.networking.WorkerClient;
import com.ahmet.search.SearchCoordinator;
import com.ahmet.search.SearchWorker;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnElectionAction implements OnElectionCallback {
    private final ServiceRegistry workersServiceRegistry;
    private final ServiceRegistry coordinatorsServiceRegistry;
    private final int port;
    private WebServer webServer;

    public OnElectionAction(ServiceRegistry workersServiceRegistry, ServiceRegistry coordinatorsServiceRegistry, int port) {
        this.workersServiceRegistry = workersServiceRegistry;
        this.coordinatorsServiceRegistry = coordinatorsServiceRegistry;
        this.port = port;
    }

    @Override
    public void onElectedToBeLeader() {
        workersServiceRegistry.unregisterFromCluster();
        workersServiceRegistry.registerForUpdates();

        SearchCoordinator searchCoordinator = new SearchCoordinator(workersServiceRegistry, new WorkerClient());
        if (webServer != null) {
            webServer.stop();
        }

        webServer = new WebServer(port, searchCoordinator);
        try {
            webServer.startServer();
        } catch (IOException e) {
            return;
        }

        // Leader is automatically assigned as the coordinator. Any dying leader will already be removed
        // from the coordinators znode hierarchy.
        registerToRegistry(coordinatorsServiceRegistry, searchCoordinator);
    }

    @Override
    public void onElectedAsWorker() {
        SearchWorker searchWorker = new SearchWorker();
        webServer = new WebServer(port, searchWorker);
        try {
            webServer.startServer();
        } catch (IOException e) {
            return;
        }

        registerToRegistry(workersServiceRegistry, searchWorker);
    }

    private void registerToRegistry(ServiceRegistry registry, OnRequestCallback onRequestCallback) {
        try {
            String currentNodeAddress = String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, onRequestCallback.getEndpoint());
            registry.registerToCluster(currentNodeAddress);
        } catch (UnknownHostException | InterruptedException | KeeperException ignored) { }
    }
}
