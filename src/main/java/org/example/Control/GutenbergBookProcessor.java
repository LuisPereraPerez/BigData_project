package org.example.Control;

import org.example.interfaces.BookProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GutenbergBookProcessor implements BookProcessor {
    private static final String SAVE_DIR = "datalake/books";

    @Override
    public void processBook(int bookId) {
        try {
            Path bookPath = Paths.get(SAVE_DIR, bookId + ".txt");
            String text = new String(Files.readAllBytes(bookPath), "UTF-8");

            // Expresión regular ajustada para coincidir con el título variable de manera no codiciosa
            Pattern startPattern = Pattern.compile("\\*\\*\\* START OF THE PROJECT GUTENBERG EBOOK .+? \\*\\*\\*");
            Pattern endPattern = Pattern.compile("\\*\\*\\* END OF THE PROJECT GUTENBERG EBOOK .+? \\*\\*\\*");

            Matcher startMatcher = startPattern.matcher(text);
            Matcher endMatcher = endPattern.matcher(text);

            if (startMatcher.find() && endMatcher.find()) {
                String rawContent = text.substring(startMatcher.end(), endMatcher.start()).trim();
                String[] lines = rawContent.split("\n");
                List<String> paragraphs = new ArrayList<>();
                StringBuilder currentParagraph = new StringBuilder();

                for (String line : lines) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        currentParagraph.append(line).append(" ");
                    } else if (currentParagraph.length() > 0) {
                        paragraphs.add(currentParagraph.toString().trim());
                        currentParagraph.setLength(0);
                    }
                }

                if (currentParagraph.length() > 0) {
                    paragraphs.add(currentParagraph.toString().trim());
                }

                String finalContent = String.join("\n\n", paragraphs);
                Files.write(bookPath, finalContent.getBytes("UTF-8"));
                System.out.println("Libro procesado y guardado: " + bookId);
            } else {
                System.out.println("No se encontraron las marcas de inicio o fin para el libro con ID " + bookId + ". El procesamiento se detiene para este libro.");
            }
        } catch (IOException e) {
            System.out.println("Error al procesar el libro con ID " + bookId + ": " + e.getMessage());
        }
    }
}
