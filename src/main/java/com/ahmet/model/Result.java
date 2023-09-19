package com.ahmet.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Result implements Serializable {
    private final Map<String, DocumentTf> documentToDocumentTf = new HashMap<>();

    public void addDocumentTf(String docName, DocumentTf documentTf) {
        documentToDocumentTf.put(docName, documentTf);
    }

    public Map<String, DocumentTf> getDocumentToDocumentTf() {
        return Collections.unmodifiableMap(documentToDocumentTf);
    }
}
