package org.example.control;

import com.google.gson.*;
import org.example.interfaces.IndexLoader;
import org.example.model.WordData;
import org.example.model.WordPosition;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class JSONIndexLoader implements IndexLoader {
    @Override
    public Map<String, Map<Integer, WordData>> loadIndex(String indexBasePath) throws Exception {
        Map<String, Map<Integer, WordData>> index = new HashMap<>();
        File baseFolder = new File(indexBasePath);

        if (!baseFolder.exists() || !baseFolder.isDirectory()) {
            throw new Exception("The base folder does not exist or is not a valid directory.");
        }

        for (File firstLetterFolder : baseFolder.listFiles(File::isDirectory)) {
            for (File secondLetterFolder : firstLetterFolder.listFiles(File::isDirectory)) {
                for (File file : secondLetterFolder.listFiles((dir, name) -> name.endsWith(".json"))) {
                    try (FileReader reader = new FileReader(file)) {
                        JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                        String word = jsonObject.get("word").getAsString();
                        JsonObject allocations = jsonObject.getAsJsonObject("allocations");

                        Map<Integer, WordData> wordDataMap = new HashMap<>();

                        for (Map.Entry<String, JsonElement> entry : allocations.entrySet()) {
                            int bookId = Integer.parseInt(entry.getKey().replace("BookID_", ""));
                            JsonObject bookData = entry.getValue().getAsJsonObject();
                            int times = bookData.get("times").getAsInt();

                            WordData wordData = new WordData(times);

                            for (JsonElement positionElement : bookData.getAsJsonArray("positions")) {
                                JsonObject position = positionElement.getAsJsonObject();
                                int line = position.get("line").getAsInt();
                                int wordIndex = position.get("wordIndex").getAsInt();
                                wordData.addPosition(new WordPosition(line, wordIndex));
                            }
                            wordDataMap.put(bookId, wordData);
                        }

                        index.put(word, wordDataMap);
                    } catch (Exception e) {
                        System.err.printf("Error processing the file %s: %s%n", file.getName(), e.getMessage());
                    }
                }
            }
        }
        return index;
    }
}
