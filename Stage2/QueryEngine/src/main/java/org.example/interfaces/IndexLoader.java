package org.example.interfaces;

import org.example.model.WordData;

import java.util.Map;

public interface IndexLoader {
    Map<String, Map<Integer, WordData>> loadIndex(String basePath) throws Exception;
}
