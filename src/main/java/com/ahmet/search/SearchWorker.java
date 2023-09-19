package com.ahmet.search;

import com.ahmet.model.Result;
import com.ahmet.model.SerializationUtils;
import com.ahmet.model.Task;
import com.ahmet.networking.OnRequestCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class SearchWorker implements OnRequestCallback {
    private static final String ENDPOINT = "/task";
    public static final Logger LOGGER = LoggerFactory.getLogger(SearchWorker.class);

    @Override
    public byte[] handleRequest(byte[] requestPayload) {
        Task task = (Task) SerializationUtils.deserialize(requestPayload);
        LOGGER.warn("Received {} documents to process.", task.getDocumentNames().size());

        Result result = null;
        try {
            result = DocumentUtils.prepareDocToDocumentTf(task);
        } catch (FileNotFoundException ignored) { }

        return SerializationUtils.serialize(result);
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }
}
