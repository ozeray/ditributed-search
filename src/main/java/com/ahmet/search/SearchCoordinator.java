package com.ahmet.search;

import com.ahmet.management.ServiceRegistry;
import com.ahmet.model.DocumentTf;
import com.ahmet.model.Result;
import com.ahmet.model.SerializationUtils;
import com.ahmet.model.Task;
import com.ahmet.model.proto.SearchModel;
import com.ahmet.networking.OnRequestCallback;
import com.ahmet.networking.WorkerClient;
import com.google.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
        try {
            SearchModel.Request request = SearchModel.Request.parseFrom(requestPayload);
            SearchModel.Response response = createResponse(request);
            return response.toByteArray();
        } catch (InvalidProtocolBufferException ignored) { }
        return SearchModel.Response.getDefaultInstance().toByteArray();
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }

    private SearchModel.Response createResponse(SearchModel.Request request) {
        String searchQuery = request.getSearchQuery();
        LOGGER.warn("Received search query: {}", searchQuery);

        SearchModel.Response.Builder responseBuilder = SearchModel.Response.newBuilder();

        List<String> workers = workersServiceRegistry.getAllServiceAddresses();
        if (workers.isEmpty()) {
            LOGGER.error("No search workers currently available.");
            return responseBuilder.build();
        }

        List<String> searchTerms = StringUtils.lineToWords(searchQuery);

        List<Task> tasks = createTasks(workers.size(), searchTerms);
        List<Result> results = sendTasksToWorkersAndReturnResults(workers, tasks);

        List<SearchModel.Response.DocumentStats> sortedDocuments = aggregateResults(results, searchTerms);
        responseBuilder.addAllRelevantDocuments(sortedDocuments);
        return responseBuilder.build();
    }

    private List<SearchModel.Response.DocumentStats> aggregateResults(List<Result> results, List<String> searchTerms) {
        Map<String, DocumentTf> allDocumentsResults = new HashMap<>();

        for (Result result : results) {
            allDocumentsResults.putAll(result.getDocumentToDocumentTf());
        }
        Map<Double, String> scoreToDocumentsSorted = TFIDFCalculator.calculateScoresSorted(searchTerms, allDocumentsResults);
        return prepareDocumentStats(scoreToDocumentsSorted);
    }

    private List<SearchModel.Response.DocumentStats> prepareDocumentStats(Map<Double, String> scoreToDocuments) {
        List<SearchModel.Response.DocumentStats> documentStats = new ArrayList<>();
        scoreToDocuments.forEach((score, doc) -> {
            SearchModel.Response.DocumentStats documentStat = SearchModel.Response.DocumentStats.newBuilder()
                    .setDocumentName(doc.split("/")[3])
                    .setScore(score)
                    .build();
            documentStats.add(documentStat);
        });
        return documentStats;
    }

    @SuppressWarnings("unchecked")
    private List<Result> sendTasksToWorkersAndReturnResults(List<String> workers, List<Task> tasks) {
        CompletableFuture<Result>[] futures = new CompletableFuture[workers.size()];
        for (int i = 0; i < workers.size(); i++) {
            String worker = workers.get(i);
            Task task = tasks.get(i);
            byte[] requestPayload = SerializationUtils.serialize(task);
            futures[i] = workerClient.sendTask(worker, requestPayload);
        }

        List<Result> results = new ArrayList<>();
        for (CompletableFuture<Result> future : futures) {
            try {
                Result result = future.get();
                results.add(result);
            } catch (InterruptedException | ExecutionException ignored) { }
        }
        LOGGER.warn("Received {}/{} results", results.size(), tasks.size());
        return results;
    }

    private List<Task> createTasks(int numberOfWorkers, List<String> searchTerms) {
        List<List<String>> workerDocuments = splitDocumentList(numberOfWorkers, documents);

        List<Task> tasks = new ArrayList<>();
        for (List<String> documentsForWorker : workerDocuments) {
            Task task = new Task(searchTerms, documentsForWorker);
            tasks.add(task);
        }
        return tasks;
    }

    private static List<List<String>> splitDocumentList(int numberOfWorkers, List<String> documents) {
        int numberOfDocumentsPerWorker = (documents.size() + numberOfWorkers - 1) / numberOfWorkers;
        List<List<String>> workerDocuments = new ArrayList<>();

        for (int i = 0; i < numberOfWorkers; i++) {
            int firstDocIndex = i * numberOfDocumentsPerWorker;
            int lastDocIndexExclusive = Math.min(firstDocIndex + numberOfDocumentsPerWorker, documents.size());
            if (firstDocIndex >= lastDocIndexExclusive) {
                break;
            }
            List<String> currentWorkerDocuments = new ArrayList<>(documents.subList(firstDocIndex, lastDocIndexExclusive));
            workerDocuments.add(currentWorkerDocuments);
        }

        return workerDocuments;
    }
}
