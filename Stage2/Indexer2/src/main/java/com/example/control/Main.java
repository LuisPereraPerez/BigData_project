package com.example.control;

import com.example.interfaces.FileHandler;
import com.example.interfaces.WordDataHandler;

public class Main {
    public static void main(String[] args) {
        FileHandler fileHandler = new TsvFileHandler();
        WordDataHandler wordDataHandler = new WordDataHandlerImpl();
        BookIndexer indexer = new BookIndexer(fileHandler, wordDataHandler);
        indexer.execute();
        System.out.println("Indexing completed successfully.");
    }
}