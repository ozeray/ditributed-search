package com.ahmet.search;

import com.ahmet.model.DocumentTf;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface TFIDFCalculator {

    static double calculateTf(String term, List<String> words) {
        return (double) words.stream().filter(word -> word.equalsIgnoreCase(term)).count() / words.size();
    }

    private static double calculateIdf(String term, Map<String, DocumentTf> docToDocumentTf) {
        int numberOfDocs = docToDocumentTf.size();
        long numOfDocsContainingTerm = docToDocumentTf.values().stream().filter(docTf -> docTf.getFrequency(term) > 0).count();
        return numOfDocsContainingTerm == 0 ? 0 : Math.log10((double) numberOfDocs / numOfDocsContainingTerm);
    }

    private static double calculateTermScorePerDocument(String term, Map<String, DocumentTf> docToDocumentTf, String docName) {
        DocumentTf documentTf = docToDocumentTf.get(docName);
        double tf = documentTf.getFrequency(term);
        double idf = calculateIdf(term, docToDocumentTf);
        return tf * idf;
    }

    private static double calculateTotalScorePerDocument(List<String> terms, Map<String, DocumentTf> docToDocumentTf, String docName) {
        return terms.stream().mapToDouble(term -> calculateTermScorePerDocument(term, docToDocumentTf, docName)).sum();
    }

    static Map<Double, String> calculateScoresSorted(List<String> terms, Map<String, DocumentTf> docToDocumentTf) {
        TreeMap<Double, String> scoreToDocName = new TreeMap<>();
        docToDocumentTf.keySet().forEach(docName -> {
            double docScore = calculateTotalScorePerDocument(terms, docToDocumentTf, docName);
            scoreToDocName.put(docScore, docName);
        });
        return scoreToDocName.descendingMap();
    }
}
