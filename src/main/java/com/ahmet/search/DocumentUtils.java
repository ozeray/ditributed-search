package com.ahmet.search;

import com.ahmet.model.DocumentTf;
import com.ahmet.model.Result;
import com.ahmet.model.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface DocumentUtils {

    static List<String> readDocumentsList(String directory) {
        File books = new File(directory);
        return Arrays.stream(Objects.requireNonNull(books.list()))
                     .map(docName -> directory + "/" + docName)
                     .toList();
    }

    private static DocumentTf createDocumentTf(List<String> terms, List<String> words) {
        DocumentTf documentTf = new DocumentTf();
        terms.forEach(term -> {
            double tf = TFIDFCalculator.calculateTf(term, words);
            documentTf.putTermFrequency(term, tf);
        });
        return documentTf;
    }

    static Result prepareDocToDocumentTf(Task task) throws FileNotFoundException {
        Result result = new Result();
        for (String docName: task.getDocumentNames()) {
            List<String> wordsInDoc = wordsInDocument(docName);
            DocumentTf documentTf = createDocumentTf(task.getSearchTerms(), wordsInDoc);
            result.addDocumentTf(docName, documentTf);
        }
        return result;
    }

    private static List<String> wordsInDocument(String docName) throws FileNotFoundException {
        File doc = new File(docName);
        BufferedReader br = new BufferedReader(new FileReader(doc));
        return br.lines()
                 .flatMap(line -> StringUtils.lineToWords(line).stream())
                 .toList();
    }
}
