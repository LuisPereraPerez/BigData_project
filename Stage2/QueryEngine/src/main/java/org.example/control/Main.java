package org.example.control;

import org.example.interfaces.IndexLoader;
import org.example.model.Metadata;
import org.example.model.WordData;

import java.util.Map;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String metadataPath = "datalake/metadata.csv";
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Do you want to load JSON or TSV indexes? (json/tsv): ");
            String option = scanner.nextLine().trim().toLowerCase();

            IndexLoader loader;
            String indexBasePath;

            if ("json".equals(option)) {
                loader = new JSONIndexLoader();
                indexBasePath = "datamart/reverse_indexes_indexer1";
            } else if ("tsv".equals(option)) {
                loader = new TSVIndexLoader();
                indexBasePath = "datamart/reverse_indexes_indexer2";
            } else {
                System.out.println("Invalid option. Program will terminate.");
                return;
            }

            System.out.println("Loading metadata...");
            CSVMetadataLoader metadataLoader = new CSVMetadataLoader();
            Map<Integer, Metadata> metadata = metadataLoader.loadMetadata(metadataPath);

            System.out.println("Loading indexes...");
            Map<String, Map<Integer, WordData>> index = loader.loadIndex(indexBasePath);

            SimpleQueryProcessor queryProcessor = new SimpleQueryProcessor(index, metadata);
            System.out.println("Query system initialized. Type a word to search:");

            while (true) {
                System.out.print("Query: ");
                String query = scanner.nextLine().trim();
                if (query.equalsIgnoreCase("exit")) {
                    break;
                }
                queryProcessor.processQuery(query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
