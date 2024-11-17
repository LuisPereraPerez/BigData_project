package org.example.control;

import org.example.interfaces.MetadataLoader;
import org.example.model.Metadata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class CSVMetadataLoader implements MetadataLoader {
    public Map<Integer, Metadata> loadMetadata(String metadataPath) throws Exception {
        Map<Integer, Metadata> metadataMap = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(metadataPath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    System.err.println("Invalid line in metadata: " + line);
                    continue;
                }

                int bookId = Integer.parseInt(parts[0].trim());
                String title = parts[1].trim();
                String author = parts[2].trim();

                Metadata metadata = new Metadata(bookId, title, author);

                metadataMap.put(bookId, metadata);
            }
        }

        return metadataMap;
    }
}
