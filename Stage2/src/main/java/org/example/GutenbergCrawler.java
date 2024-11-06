package org.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class GutenbergCrawler {

    private static final String BASE_URL = "https://www.gutenberg.org/ebooks/";
    private static final int NUM_BOOKS = 20;
    private static final String SAVE_DIR = "datalake/books";
    private static final String LAST_ID_FILE = "src/main/resources/last_id.txt"; // Ubicación en resources
    private static final String METADATA_CSV_FILE = "datalake/metadata.csv"; // Ahora guardado en datalake

    public static void main(String[] args) {
        // Crear directorio de guardado si no existe
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Crear el archivo CSV para los metadatos si no existe
        File metadataFile = new File(METADATA_CSV_FILE);
        if (!metadataFile.exists()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(METADATA_CSV_FILE))) {
                writer.write("ID,Title,Author,Release Date,Most Recently Updated,Language"); // Encabezados del CSV
                writer.newLine();
            } catch (IOException e) {
                System.out.println("No se pudo crear el archivo CSV de metadatos.");
            }
        }

        // Leer el último ID descargado desde el archivo, si existe
        int startId = getLastDownloadedId() + 1;

        // Descargar los próximos 5 libros
        for (int i = startId; i < startId + NUM_BOOKS; i++) {
            try {
                downloadBook(i);
                // Obtener metadatos y añadirlos al CSV
                Map<String, String> metadata = obtainMetadata(i);
                if (metadata != null) {
                    appendMetadataToCSV(metadata);
                }
                processingBook(i);
            } catch (IOException e) {
                System.out.println("Error al descargar el libro con ID: " + i);
            }
        }

        // Actualizar el archivo con el último ID descargado
        updateLastDownloadedId(startId + NUM_BOOKS - 1);
    }

    // Método para descargar el libro
    private static void downloadBook(int bookId) throws IOException {
        String url = BASE_URL + bookId;  // URL del libro
        Document doc = Jsoup.connect(url).get();

        // Buscar el enlace al archivo de texto (Plain Text UTF-8)
        String textLink = getTextLink(doc);

        if (textLink != null) {
            // Descargar el contenido del archivo de texto
            String content = Jsoup.connect(textLink).ignoreContentType(true).execute().body();  // Esto descarga el archivo de texto

            // Crear el directorio de destino si no existe
            File dir = new File(SAVE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Guardar el libro en un archivo .txt con solo el ID
            String bookFileName = SAVE_DIR + "/" + bookId + ".txt";
            try (PrintWriter writer = new PrintWriter(new File(bookFileName))) {
                writer.write(content);
            }

            System.out.println("Libro con ID " + bookId + " descargado.");
        } else {
            System.out.println("El libro con ID " + bookId + " no tiene un archivo de texto disponible.");
        }
    }

    // Método para obtener el enlace al archivo de texto en formato UTF-8
    private static String getTextLink(Document doc) {
        Element link = doc.select("a[href]").stream()
                .filter(a -> a.text().equals("Plain Text UTF-8")) // Buscar enlace al texto
                .findFirst()
                .orElse(null);
        if (link != null) {
            return "https://www.gutenberg.org" + link.attr("href"); // Construir URL completa
        }
        return null;
    }

    // Método para obtener los metadatos del libro
    private static Map<String, String> obtainMetadata(int bookId) {
        Map<String, String> metadata = new HashMap<>();
        File bookFile = new File(SAVE_DIR + "/" + bookId + ".txt");

        try {
            // Leer el contenido del archivo de libro
            String text = new String(Files.readAllBytes(Paths.get(bookFile.toURI())), "UTF-8");

            // Usar expresiones regulares para extraer los metadatos
            metadata.put("ID", String.valueOf(bookId));
            metadata.put("Title", extractMetadata("Title: (.+)", text));
            metadata.put("Author", extractMetadata("Author: (.+)", text));
            metadata.put("Release Date", extractMetadata("Release Date: (.+)", text));
            metadata.put("Most Recently Updated", extractMetadata("Most recently updated: (.+)", text));
            metadata.put("Language", extractMetadata("Language: (.+)", text));

            return metadata;
        } catch (IOException e) {
            System.out.println("Error al leer el libro con ID " + bookId + ": " + e.getMessage());
            return null;
        }
    }

    // Método para extraer los metadatos usando expresión regular
    private static String extractMetadata(String regex, String text) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Unknown"; // Si no se encuentra el dato, devolver "Unknown"
    }

    // Método para añadir los metadatos al archivo CSV en el orden correcto
    private static void appendMetadataToCSV(Map<String, String> metadata) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(METADATA_CSV_FILE, true))) {
            String csvLine = String.join(",",
                    metadata.get("ID"),
                    metadata.get("Title"),
                    metadata.get("Author"),
                    metadata.get("Release Date"),
                    metadata.get("Most Recently Updated"),
                    metadata.get("Language")
            );
            writer.write(csvLine);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al escribir los metadatos en el archivo CSV.");
        }
    }

    // Método para obtener el último ID descargado desde el archivo
    private static int getLastDownloadedId() {
        try {
            File file = new File(LAST_ID_FILE);
            if (file.exists()) {
                String lastId = new String(Files.readAllBytes(Paths.get(LAST_ID_FILE)));
                return Integer.parseInt(lastId.trim());
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de ID, comenzando desde 0.");
        }
        return 0; // Si no existe el archivo, empezamos desde el ID 0
    }

    // Método para actualizar el último ID descargado en el archivo
    private static void updateLastDownloadedId(int lastId) {
        try {
            Files.write(Paths.get(LAST_ID_FILE), String.valueOf(lastId).getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("No se pudo actualizar el archivo de ID.");
        }
    }

    // Método para procesar el libro y extraer solo el contenido principal
    public static void processingBook(int idBook) {
        try {
            // Leer el archivo del libro
            Path bookPath = Paths.get(SAVE_DIR, idBook + ".txt");
            String text = new String(Files.readAllBytes(bookPath), "UTF-8");

            // Buscar las marcas de inicio y fin del contenido del libro
            Pattern startPattern = Pattern.compile("\\*\\*\\* START OF THE PROJECT .+ \\*\\*\\*");
            Pattern endPattern = Pattern.compile("\\*\\*\\* END OF THE PROJECT .+ \\*\\*\\*");

            Matcher startMatcher = startPattern.matcher(text);
            Matcher endMatcher = endPattern.matcher(text);

            if (startMatcher.find() && endMatcher.find()) {
                // Extraer el contenido entre las marcas de inicio y fin
                String rawContent = text.substring(startMatcher.end(), endMatcher.start()).trim();

                // Procesar el contenido para agregar saltos de línea entre párrafos
                String[] lines = rawContent.split("\n");
                List<String> paragraphs = new ArrayList<>();
                StringBuilder currentParagraph = new StringBuilder();

                for (String line : lines) {
                    line = line.trim();  // Eliminar espacios en blanco adicionales
                    if (!line.isEmpty()) {
                        currentParagraph.append(line).append(" ");
                    } else {
                        if (currentParagraph.length() > 0) {
                            paragraphs.add(currentParagraph.toString().trim());
                            currentParagraph.setLength(0); // Reset para el siguiente párrafo
                        }
                    }
                }

                // Agregar el último párrafo si no está vacío
                if (currentParagraph.length() > 0) {
                    paragraphs.add(currentParagraph.toString().trim());
                }

                // Unir los párrafos procesados con saltos de línea
                String finalContent = String.join("\n\n", paragraphs);

                // Guardar el contenido procesado en un nuevo archivo
                Path processedBookPath = Paths.get(SAVE_DIR, idBook + ".txt");
                Files.write(processedBookPath, finalContent.getBytes("UTF-8"));

                System.out.println("Libro procesado y guardado: " + idBook);
            } else {
                System.out.println("No se encontraron las marcas de inicio o fin del contenido para el libro con ID " + idBook);
            }
        } catch (IOException e) {
            System.out.println("Error al procesar el libro con ID " + idBook + ": " + e.getMessage());
        }
    }

}
