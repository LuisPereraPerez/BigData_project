package org.example.control;

import org.example.interfaces.QueryProcessor;
import org.example.model.Metadata;
import org.example.model.WordData;
import org.example.model.WordPosition;

import java.util.Map;

public class SimpleQueryProcessor implements QueryProcessor {
    private final Map<String, Map<Integer, WordData>> index;
    private final Map<Integer, Metadata> metadata;

    public SimpleQueryProcessor(Map<String, Map<Integer, WordData>> index, Map<Integer, Metadata> metadata) {
        this.index = index;
        this.metadata = metadata;
    }

    public void processQuery(String query) {
        query = query.toLowerCase();
        Map<Integer, WordData> results = index.get(query);

        if (results == null || results.isEmpty()) {
            System.out.printf("No results found for the word: %s%n", query);
            return;
        }

        System.out.println("Results found for the word: " + query);
        for (Map.Entry<Integer, WordData> entry : results.entrySet()) {
            int bookId = entry.getKey();
            WordData wordData = entry.getValue();
            Metadata bookMetadata = metadata.get(bookId);

            if (bookMetadata != null) {
                System.out.printf("Book: %s | Author: %s | Total occurrences: %d%n",
                        bookMetadata.getTitle(),
                        bookMetadata.getAuthor(),
                        wordData.getTotalOccurrences());
            } else {
                System.out.printf("Book ID: %d ", bookId);
            }

            for (WordPosition position : wordData.getPositions()) {
                System.out.printf("  - Line: %d\n", position.getLine());
            }
        }
    }
}
