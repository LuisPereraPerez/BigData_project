package com.example.control;

import com.example.interfaces.MetadataWriter;

import java.io.*;
import java.util.Map;

public class CSVMetadataWriter implements MetadataWriter {
    private static final String METADATA_CSV_FILE = "datalake/metadata.csv";
    private static final String HEADER = "ID,Title,Author,Release Date,Most Recently Updated,Language";

    private String escapeForCSV(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    @Override
    public void writeMetadata(Map<String, String> metadata) {
        File file = new File(METADATA_CSV_FILE);
        boolean isNewFile = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            if (isNewFile) {
                writer.write(HEADER);
                writer.newLine();
            }

            String csvLine = String.join(",",
                    metadata.getOrDefault("ID", ""),
                    escapeForCSV(metadata.getOrDefault("Title", "")),
                    escapeForCSV(metadata.getOrDefault("Author", "")),
                    escapeForCSV(metadata.getOrDefault("Release Date", "")),
                    escapeForCSV(metadata.getOrDefault("Most Recently Updated", "")),
                    escapeForCSV(metadata.getOrDefault("Language", "")));

            if (!isDuplicate(csvLine)) {
                writer.write(csvLine);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isDuplicate(String csvLine) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(METADATA_CSV_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(csvLine)) {
                    return true;
                }
            }
        }
        return false;
    }
}
