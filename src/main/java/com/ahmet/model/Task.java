package com.ahmet.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class Task implements Serializable {
    private final List<String> searchTerms;
    private final List<String> documentNames;

    public Task(List<String> searchTerms, List<String> documentNames) {
        this.searchTerms = searchTerms;
        this.documentNames = documentNames;
    }

    public List<String> getSearchTerms() {
        return Collections.unmodifiableList(searchTerms);
    }

    public List<String> getDocumentNames() {
        return Collections.unmodifiableList(documentNames);
    }
}
