package com.example.control;

import com.example.interfaces.FileHandler;

import java.io.*;
import java.util.*;

public class TsvFileHandler implements FileHandler {

    @Override
    public List<String> loadBooks() {
        // Logic to load the books (as described previously)
        File folder = new File("./datalake/books/"); // Path to the folder containing the books
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));
        List<String> bookFiles = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                bookFiles.add(file.getPath());
            }
        }
        return bookFiles;
    }

    @Override
    public List<String> readLines(String bookFilePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(bookFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    // Exception handling within the method without declaring throws IOException
    public void saveWordsToFile(String word, String bookId, int paragraphIndex, int count) {
        // Extraer solo el ID del libro desde bookId
        String[] pathParts = bookId.split("[/\\\\]"); // Divide por / o \ según el sistema operativo
        String bookIdOnly = pathParts[pathParts.length - 1]; // Obtiene la última parte como ID del libro

        // Crear la ruta de directorio usando las primeras dos letras de la palabra
        String subfolder = word.length() > 1 ? word.substring(0, 2) : word.substring(0, 1);
        String directoryPath = "datamart/reverse_indexes_Indexer2/" + subfolder.charAt(0) + "/" + subfolder;

        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = directoryPath + "/" + word + ".tsv";
        File file = new File(filePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            // Si el archivo no existe, escribir la cabecera antes de cualquier dato
            if (file.length() == 0) { // Verificar si el archivo está vacío
                writer.write("Book_ID\tLine\tOccurrences");
                writer.newLine();
            }

            // Crear la línea a añadir con el formato requerido
            String lineToAdd = bookIdOnly + "\t" + paragraphIndex + "\t" + count;

            // Escribir la línea de datos
            writer.write(lineToAdd);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error while writing to the TSV file: " + filePath);
            e.printStackTrace();
        }
    }


}
