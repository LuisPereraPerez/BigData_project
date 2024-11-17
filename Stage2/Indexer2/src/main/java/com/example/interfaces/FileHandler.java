package com.example.interfaces;

import java.util.List;

public interface FileHandler {
    List<String> readLines(String filePath);
    List<String> loadBooks();
}
