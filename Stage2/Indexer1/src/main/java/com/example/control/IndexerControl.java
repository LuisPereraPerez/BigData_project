package com.example.control;

import com.example.interfaces.*;
import com.example.model.BookAllocation;
import com.example.model.Position;
import com.example.model.Word;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class IndexerControl implements BookIndexer {

    private final LastBookManager lastBookManager;
    private final JsonFileManager jsonFileManager;
    private final WordCleaner wordCleaner;
    private final WordLemmatizer wordLemmatizer;

    public IndexerControl() {
        this.lastBookManager = new BookManagerControl();
        this.jsonFileManager = new JsonFileManagerControl();
        this.wordCleaner = new WordCleanerControl();
        this.wordLemmatizer = new WordLemmatizerControl();
    }

    @Override
    public void indexBook(int bookId) throws IOException {
        String bookFilePath = "datalake/books/" + bookId + ".txt";

        List<String> lines = Files.readAllLines(Paths.get(bookFilePath));
        Map<String, Word> wordMap = new HashMap<>();

        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber);
            String[] words = line.split("\\s+");

            for (int position = 0; position < words.length; position++) {
                String cleanedWord = wordCleaner.cleanWord(words[position]);
                String lemma = wordLemmatizer.lemmatize(cleanedWord);

                if (!lemma.isEmpty()) {
                    Position pos = new Position(lineNumber + 1, position + 1);
                    String bookKey = "BookID_" + bookId;

                    wordMap.computeIfAbsent(lemma, k -> new Word(lemma, new HashMap<>(), 0))
                            .getAllocations()
                            .computeIfAbsent(bookKey, k -> new BookAllocation(0, new ArrayList<>()))
                            .getPositions().add(pos);

                    wordMap.get(lemma).getAllocations().get(bookKey).setTimes(
                            wordMap.get(lemma).getAllocations().get(bookKey).getTimes() + 1);
                    wordMap.get(lemma).setTotal(wordMap.get(lemma).getTotal() + 1);
                }
            }
        }

        for (String lemma : wordMap.keySet()) {
            saveOrUpdateWord(wordMap.get(lemma));
        }
    }

    private void saveOrUpdateWord(Word word) throws IOException {
        String wordText = word.getWord().toLowerCase();

        Set<String> reservedWindowsWords = Set.of(
                "con", "prn", "aux", "nul",
                "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
                "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
        );

        if (reservedWindowsWords.contains(wordText)) {
            System.out.println("Skipping reserved word: " + wordText);
            return;
        }

        String firstLetter = wordText.substring(0, 1);
        String twoFirstsLetter = wordText.length() > 1 ? wordText.substring(0, 2) : firstLetter;

        String directoryPath = "datamart/reverse_indexes_Indexer1/" + firstLetter + "/" + twoFirstsLetter;
        Files.createDirectories(Paths.get(directoryPath));

        String jsonFilePath = directoryPath + "/" + word.getWord() + ".json";
        Word existingWord = jsonFileManager.readJson(jsonFilePath);

        if (existingWord != null) {
            mergeWordData(existingWord, word);
            jsonFileManager.writeJson(jsonFilePath, existingWord);
        } else {
            jsonFileManager.writeJson(jsonFilePath, word);
        }
    }


    private void mergeWordData(Word existingWord, Word newWord) {
        for (Map.Entry<String, BookAllocation> entry : newWord.getAllocations().entrySet()) {
            String bookKey = entry.getKey();
            BookAllocation newBookAllocation = entry.getValue();

            existingWord.getAllocations().merge(bookKey, newBookAllocation, (existingAlloc, newAlloc) -> {
                Set<Position> positionSet = new HashSet<>(existingAlloc.getPositions());
                positionSet.addAll(newAlloc.getPositions());
                existingAlloc.setPositions(new ArrayList<>(positionSet));
                existingAlloc.setTimes(positionSet.size());
                return existingAlloc;
            });

            existingWord.setTotal(existingWord.getTotal() + newBookAllocation.getTimes());
        }
    }

    public void executeIndexing() throws IOException {
        String lastBookPath = "resources/lastBookId_indexer1.txt";
        int lastProcessedBookId = lastBookManager.readLastProcessedBookId(lastBookPath);

        Path booksDirectory = Paths.get("datalake/books");
        try (Stream<Path> bookFiles = Files.list(booksDirectory)) {
            bookFiles
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().matches("\\d+\\.txt"))
                    .sorted(Comparator.comparingInt(path -> Integer.parseInt(path.getFileName().toString().replace(".txt", ""))))
                    .filter(path -> Integer.parseInt(path.getFileName().toString().replace(".txt", "")) > lastProcessedBookId)
                    .forEach(path -> {
                        try {
                            int bookId = Integer.parseInt(path.getFileName().toString().replace(".txt", ""));
                            indexBook(bookId);
                            lastBookManager.updateLastProcessedBookId(lastBookPath, bookId);
                        } catch (IOException e) {
                            System.err.println("Error indexing book: " + path.getFileName());
                            e.printStackTrace();
                        }
                    });
        }
    }
}

