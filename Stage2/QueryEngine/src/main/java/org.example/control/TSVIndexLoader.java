package org.example.control;

import org.example.interfaces.IndexLoader;
import org.example.model.WordData;
import org.example.model.WordPosition;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class TSVIndexLoader implements IndexLoader {
    @Override
    public Map<String, Map<Integer, WordData>> loadIndex(String directoryPath) throws Exception {
        Map<String, Map<Integer, WordData>> index = new HashMap<>();
        File directory = new File(directoryPath);

        for (File firstLetter : directory.listFiles(File::isDirectory)) {
            for (File secondLetter : firstLetter.listFiles(File::isDirectory)) {
                for (File file : secondLetter.listFiles((f) -> f.getName().endsWith(".tsv"))) {
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String word = file.getName().replace(".tsv", "");
                        Map<Integer, WordData> wordDataMap = new HashMap<>();
                        String line;

                        reader.readLine(); // Skip header line

                        while ((line = reader.readLine()) != null) {
                            String[] parts = line.split("\t");

                            if (parts.length < 3) {
                                System.err.printf("Skipping invalid line in file %s: %s%n", file.getName(), line);
                                continue;
                            }

                            int bookId = Integer.parseInt(parts[0]);
                            int lineNumber = Integer.parseInt(parts[1]);
                            int occurrences = Integer.parseInt(parts[2]);

                            WordData wordData = wordDataMap.getOrDefault(bookId, new WordData(0));

                            // Add each occurrence as a separate position
                            for (int i = 0; i < occurrences; i++) {
                                wordData.incrementTotalOccurrences(1);
                                wordData.addPosition(new WordPosition(lineNumber, i + 1)); // Positions start at 1
                            }

                            wordDataMap.put(bookId, wordData);
                        }

                        index.put(word, wordDataMap);
                    }
                }
            }
        }
        return index;
    }
}
