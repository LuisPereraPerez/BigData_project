package com.example.control;

import com.example.interfaces.FileHandler;
import com.example.interfaces.Indexer;
import com.example.interfaces.WordDataHandler;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BookIndexer implements Indexer {
    private final FileHandler fileHandler;
    private TsvFileHandler tsvFileHandler;
    private final WordDataHandler wordDataHandler;
    private final Set<String> indexedBooks;

    public BookIndexer(FileHandler fileHandler, WordDataHandler wordDataHandler) {
        this.fileHandler = fileHandler;
        this.wordDataHandler = wordDataHandler;
        this.indexedBooks = loadIndexedBooks();  // Load previously indexed books
    }

    @Override
    public void execute() {
        try {
            // Obtener el último ID procesado
            String lastIndexedBookId = getLastIndexedBookId();
            int lastProcessedId = lastIndexedBookId.isEmpty() ? 0 : Integer.parseInt(lastIndexedBookId);
            System.out.println("Last indexed book ID: " + lastProcessedId);

            // Cargar todos los archivos de libros
            List<String> bookFiles = fileHandler.loadBooks();

            // Filtrar libros por aquellos cuyo ID sea mayor al último procesado
            bookFiles.removeIf(bookFile -> {
                String bookId = getBookId(bookFile);
                int currentBookId = Integer.parseInt(bookId);
                return currentBookId <= lastProcessedId;
            });

            // Procesar los libros restantes
            for (String bookFile : bookFiles) {
                String bookId = getBookId(bookFile);
                List<String> paragraphs = fileHandler.readLines(bookFile);
                processBook(bookId, paragraphs);

                // Marcar el libro como indexado
                indexedBooks.add(bookId);

                // Guardar el ID del libro procesado
                saveIndexedBooks();
                System.out.println("Book " + bookId + " indexed successfully.");
            }
        } catch (Exception e) { // Manejo genérico de excepciones
            System.out.println("Error during execution: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getLastIndexedBookId() {
        String filePath = "resources/lastBookId_indexer2.txt";
        File file = new File(filePath);

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                return reader.readLine().trim(); // Retorna la primera línea del archivo
            } catch (IOException e) {
                System.out.println("Error reading the last indexed book ID: " + e.getMessage());
            }
        }
        return ""; // Retorna vacío si el archivo no existe
    }

    private void processBook(String bookId, List<String> paragraphs) {
        tsvFileHandler = new TsvFileHandler();

        // Lista de palabras reservadas de Windows
        Set<String> reservedWindowsWords = Set.of(
                "con", "prn", "aux", "nul",
                "com1", "com2", "com3", "com4", "com5", "com6", "com7", "com8", "com9",
                "lpt1", "lpt2", "lpt3", "lpt4", "lpt5", "lpt6", "lpt7", "lpt8", "lpt9"
        );

        for (int paragraphIndex = 0; paragraphIndex < paragraphs.size(); paragraphIndex++) {
            String paragraph = paragraphs.get(paragraphIndex);

            // Obtener y limpiar las palabras del párrafo
            List<String> words = wordDataHandler.cleanAndSplit(paragraph);

            // Limpiar cada palabra y lematizar
            for (int i = 0; i < words.size(); i++) {
                String word = words.get(i);

                // Verificar si la palabra es reservada
                if (reservedWindowsWords.contains(word.toLowerCase())) {
                    System.out.println("Skipping reserved word: " + word);
                    continue; // Saltar la palabra si es reservada
                }

                word = wordDataHandler.cleanWord(word); // Limpia la palabra
                word = wordDataHandler.lemmAdd(word);   // Lematiza la palabra
                words.set(i, word);
            }

            // Mapa para contar la ocurrencia de palabras en este párrafo
            Map<String, Integer> wordCountMap = new HashMap<>();

            for (String word : words) {
                if (!word.isEmpty() && !reservedWindowsWords.contains(word.toLowerCase())) {
                    // Actualizar el conteo de palabras en el mapa
                    wordCountMap.put(word, wordCountMap.getOrDefault(word, 0) + 1);
                }
            }

            // Guardar todas las palabras con su conteo final en el archivo TSV
            for (Map.Entry<String, Integer> entry : wordCountMap.entrySet()) {
                String word = entry.getKey();
                int count = entry.getValue();

                // Verificar si la palabra es reservada antes de guardarla
                if (reservedWindowsWords.contains(word.toLowerCase())) {
                    System.out.println("Skipping reserved word during saving: " + word);
                    continue; // Saltar la palabra si es reservada
                }

                try {
                    // Guardar la palabra en el archivo TSV con el conteo final
                    tsvFileHandler.saveWordsToFile(word, bookId, paragraphIndex + 1, count);
                } catch (Exception e) {
                    System.out.println("Error while saving the word to the TSV file: " + e.getMessage());
                }
            }
        }
    }


    private String getBookId(String bookFilePath) {
        String fileName = new File(bookFilePath).getName(); // Extraer solo el nombre del archivo
        return fileName.replace(".txt", ""); // Eliminar la extensión .txt
    }

    // Load the list of indexed books from a file
    private Set<String> loadIndexedBooks() {
        Set<String> indexedBooks = new HashSet<>();

        // Ruta relativa a la carpeta resources
        // Indexer2/resources/lastBookId.txt Local usage
        String filePath = "resources/lastBookId_indexer2.txt";
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
        // Indexer2/resources Local usage
        String directoryPath = "resources";
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = directoryPath + "/lastBookId_indexer2.txt";
        File indexedBooksFile = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(indexedBooksFile))) {
            if (!indexedBooks.isEmpty()) {
                // Obtener el último ID en el conjunto (usando el último por orden natural)
                String lastBookId = indexedBooks.stream()
                        .map(bookId -> bookId.replaceAll("\\D+", "")) // Extraer solo números del bookId
                        .max(String::compareTo) // Obtener el máximo por orden natural
                        .orElse("");

                // Escribir solo el último ID extraído en el archivo
                writer.write(lastBookId);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error al guardar el último ID del libro: " + e.getMessage());
        }
    }

}
