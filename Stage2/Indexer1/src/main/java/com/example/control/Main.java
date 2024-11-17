package com.example.control;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        IndexerControl indexer = new IndexerControl();
        try {
            indexer.executeIndexing();
            System.out.println("Indexing completed successfully.");
        } catch (IOException e) {
            System.err.println("An error occurred during the indexing process:");
            e.printStackTrace();
        }
    }
}
