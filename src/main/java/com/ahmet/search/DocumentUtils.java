package com.ahmet.search;

import com.ahmet.model.DocumentTf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DocumentUtils {

    static DocumentTf createDocumentTf(List<String> terms, List<String> words) {
        DocumentTf documentTf = new DocumentTf();
        terms.forEach(term -> {
            double tf = TFIDFCalculator.calculateTf(term, words);
            documentTf.putTermFrequency(term, tf);
        });
        return documentTf;
    }

    static Map<String, DocumentTf> prepareDocToDocumentTf(List<String> terms, List<String> docNames) throws FileNotFoundException {
        Map<String, DocumentTf> docToDocumentTf = new HashMap<>();
        for (String docName: docNames) {
            List<String> wordsInDoc = wordsInDocument(docName);
            DocumentTf documentTf = createDocumentTf(terms, wordsInDoc);
            docToDocumentTf.put(docName, documentTf);
        }
        return docToDocumentTf;
    }

    private static List<String> wordsInDocument(String docName) throws FileNotFoundException {
        File doc = new File(docName);
        BufferedReader br = new BufferedReader(new FileReader(doc));
        return br.lines()
                 .flatMap(line -> StringUtils.lineToWords(line).stream())
                 .toList();
    }
}
