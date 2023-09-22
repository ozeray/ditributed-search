package com.ahmet;

import com.ahmet.management.LeaderElection;
import com.ahmet.management.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class Application implements Watcher {
    private static Logger logger;
    private static String zookeeperAddress;
    private static final int SESSION_TIMEOUT = 3000;
    private static final int DEFAULT_PORT = 8081;
    private ZooKeeper zooKeeper;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        logger = LoggerFactory.getLogger(Application.class);
        int currentNodePort = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        String localhostAddress = args.length > 1 && "docker".equals(args[1]) ? "host.docker.internal" : "localhost";
        zookeeperAddress = localhostAddress + ":2181";

        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();

        createAndStartLeaderElectionService(zooKeeper, currentNodePort);

        application.run();
        application.close();
        logger.warn("Disconnected from Zookeeper server, existing application");
    }

    private static void createAndStartLeaderElectionService(ZooKeeper zooKeeper, int currentNodePort) throws InterruptedException, KeeperException {
        ServiceRegistry workersServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.WORKERS_SERVICE_REGISTRY);
        ServiceRegistry coordinatorsServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.COORDINATORS_SERVICE_REGISTRY);
        OnElectionAction onElectionAction = new OnElectionAction(workersServiceRegistry, coordinatorsServiceRegistry, currentNodePort);

        LeaderElection leaderElection = new LeaderElection(zooKeeper, onElectionAction);
        leaderElection.volunteerForLeadership();
        leaderElection.electLeader();
    }

    private ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(zookeeperAddress, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    private void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        if (Objects.requireNonNull(event.getType()) == Event.EventType.None) {
            if (Event.KeeperState.SyncConnected.equals(event.getState())) {
                logger.warn("Connected to Zookeeper Server");
            } else {
                synchronized (zooKeeper) {
                    logger.warn("Event: Disconnected from Zookeeper");
                    zooKeeper.notifyAll();
                }
            }
        }
    }
}
