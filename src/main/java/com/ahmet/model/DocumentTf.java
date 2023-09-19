package com.ahmet.model;

import java.util.HashMap;
import java.util.Map;

public class DocumentTf {

    private final Map<String, Double> map = new HashMap<>();

    public void putTermFrequency(String term, double frequency) {
        map.put(term, frequency);
    }

    public double getFrequency(String term) {
        return map.get(term);
    }
}
