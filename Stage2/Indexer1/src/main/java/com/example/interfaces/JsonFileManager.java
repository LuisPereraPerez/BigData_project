package com.example.interfaces;

import com.example.model.Word;
import java.io.IOException;

public interface JsonFileManager {
    Word readJson(String filePath) throws IOException;

    void writeJson(String filePath, Word word) throws IOException;
}
