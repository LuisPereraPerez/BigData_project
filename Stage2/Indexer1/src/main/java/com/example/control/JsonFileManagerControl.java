package com.example.control;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.interfaces.JsonFileManager;
import com.example.model.Word;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonFileManagerControl implements JsonFileManager {

    private final Gson gson;

    public JsonFileManagerControl() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    @Override
    public Word readJson(String filePath) throws IOException {
        File file = new File(filePath);

        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                return gson.fromJson(reader, Word.class);
            }
        }
        return null;
    }

    @Override
    public void writeJson(String filePath, Word word) throws IOException {
        File file = new File(filePath);

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(word, writer);
        }
    }
}
