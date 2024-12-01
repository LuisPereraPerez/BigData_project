package com.example.control;

import com.example.interfaces.FileHandler;
import com.example.interfaces.Indexer;
import com.example.interfaces.WordDataHandler;

import java.io.*;
import java.util.*;

public class BookIndexer implements Indexer {
    private final FileHandler fileHandler;
    private TsvFileHandler tsvFileHandler;
    private final WordDataHandler wordDataHandler;
    private final Set<String> indexedBooks;

    public BookIndexer(FileHandler fileHandler, WordDataHandler wordDataHandler) {
        this.fileHandler = fileHandler;
        this.wordDataHandler = wordDataHandler;
        this.indexedBooks = loadIndexedBooks();
    }

    @Override
    public void execute() {
        try {
            String lastIndexedBookId = getLastIndexedBookId();
            int lastProcessedId = lastIndexedBookId.isEmpty() ? 0 : Integer.parseInt(lastIndexedBookId);
            System.out.println("Last indexed book ID: " + lastProcessedId);

            List<String> bookFiles = fileHandler.loadBooks();

            bookFiles.sort(Comparator.comparingInt(bookFile -> Integer.parseInt(getBookId(bookFile))));

            // Remove books that have already been indexed (based on the last indexed book ID)
            bookFiles.removeIf(bookFile -> {
                String bookId = getBookId(bookFile);
                int currentBookId = Integer.parseInt(bookId);
                return currentBookId <= lastProcessedId;
            });

            for (String bookFile : bookFiles) {
                String bookId = getBookId(bookFile);
                List<String> paragraphs = fileHandler.readLines(bookFile);
                processBook(bookId, paragraphs);

                indexedBooks.add(bookId);

                saveIndexedBooks();
                System.out.println("Book " + bookId + " indexed successfully.");
            }
        } catch (Exception e) {
            System.out.println("Error during execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getLastIndexedBookId() {
        String filePath = "Indexer2/resources/lastBookId_indexer2.txt";
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine().trim();
            } catch (IOException e) {
                System.out.println("Error reading the last indexed book ID: " + e.getMessage());
            }
        }
        return "";
    }

    private void processBook(String bookId, List<String> paragraphs) {
        tsvFileHandler = new TsvFileHandler();

        Set<String> reservedWindowsWords = Set.of(
                "con", "prn", "aux", "nul",
                "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
                "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
        );

        for (int paragraphIndex = 0; paragraphIndex < paragraphs.size(); paragraphIndex++) {
            String paragraph = paragraphs.get(paragraphIndex);

            List<String> words = wordDataHandler.cleanAndSplit(paragraph);

            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);

                if (reservedWindowsWords.contains(word.toLowerCase())) {
                    System.out.println("Skipping reserved word: " + word);
                    continue;
                }

                word = wordDataHandler.cleanWord(word);
                word = wordDataHandler.lemmAdd(word);
                words.set(i, word);
            }

            Map<String, Integer> wordCountMap = new HashMap<>();

            for (String word : words) {
                if (!word.isEmpty() && !reservedWindowsWords.contains(word.toLowerCase())) {
                    wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                }
            }

            for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
                String word = entry.getKey();
                int count = entry.getValue();

                if (reservedWindowsWords.contains(word.toLowerCase())) {
                    System.out.println("Skipping reserved word during saving: " + word);
                    continue;
                }

                try {
                    tsvFileHandler.saveWordsToFile(word, bookId, paragraphIndex + 1, count);
                } catch (Exception e) {
                    System.out.println("Error while saving the word to the TSV file: " + e.getMessage());
                }
            }
        }
    }


    private String getBookId(String bookFilePath) {
        String fileName = new File(bookFilePath).getName();
        return fileName.replace(".txt", "");
    }

    // Load the list of indexed books from a file
    private Set<String> loadIndexedBooks() {
        Set<String> indexedBooks = new HashSet<>();
        String filePath = "Indexer2/resources/lastBookId_indexer2.txt";
        File indexedBooksFile = new File(filePath);
        if (indexedBooksFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(indexedBooksFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    indexedBooks.add(line.trim());
                }
            } catch (IOException e) {
                System.out.println("Error al cargar los libros indexados: " + e.getMessage());
            }
        }

        return indexedBooks;
    }

    private void saveIndexedBooks() {
        String directoryPath = "Indexer2/resources";
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = directoryPath + "/lastBookId_indexer2.txt";
        File indexedBooksFile = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexedBooksFile))) {
            if (!indexedBooks.isEmpty()) {

                String lastBookId = indexedBooks.stream()
                        .map(bookId -> bookId.replaceAll("\\D+", ""))
                        .filter(id -> !id.isEmpty())
                        .mapToInt(Integer::parseInt)
                        .max()
                        .stream()
                        .mapToObj(String::valueOf)
                        .findFirst()
                        .orElse("");

                writer.write(lastBookId);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar el último ID del libro: " + e.getMessage());
        }
    }

}
