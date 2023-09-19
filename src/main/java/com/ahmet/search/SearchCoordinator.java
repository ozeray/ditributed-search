package com.ahmet.search;

import com.ahmet.management.ServiceRegistry;
import com.ahmet.networking.OnRequestCallback;
import com.ahmet.networking.WorkerClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

// Used by frontend server to send search query.
// This will split tasks and send requests to worker nodes, finally aggregate and sort the results, and
// respond back to the front end server.
public class SearchCoordinator implements OnRequestCallback {
    private static final String BOOKS_DIRECTORY = "./resources/books";
    private static final String ENDPOINT = "/search";
    public static final Logger LOGGER = LoggerFactory.getLogger(SearchCoordinator.class);
    private final ServiceRegistry workersServiceRegistry;
    private final WorkerClient workerClient;
    private final List<String> documents;

    public SearchCoordinator(ServiceRegistry workersServiceRegistry, WorkerClient workerClient) {
        this.workersServiceRegistry = workersServiceRegistry;
        this.workerClient = workerClient;
        this.documents = DocumentUtils.readDocumentsList(BOOKS_DIRECTORY);
    }

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        return new byte[0];
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }

    private static List<List<String>> splitDocumentList(int numberOfWorkers, List<String> documents) {
        int numberOfDocumentsPerWorker = (documents.size() + numberOfWorkers - 1) / numberOfWorkers;
        List<List<String>> workerDocuments = new ArrayList<>();

        for (int i = 0; i < numberOfWorkers; i++) {
            int firstDocIndex = i * numberOfDocumentsPerWorker;
            int lastDocIndexExclusive = Math.min(firstDocIndex + numberOfWorkers, documents.size());

        }

        return workerDocuments;
    }
}
