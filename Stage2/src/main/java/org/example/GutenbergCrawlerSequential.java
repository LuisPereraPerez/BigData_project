package org.example;

import org.example.implementations.*;
import org.example.interfaces.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class GutenbergCrawlerSequential {

    private static final int NUM_BOOKS = 10;
    private static final String SAVE_DIR = "datalake/books";
    private static final int MAX_DOWNLOAD_ATTEMPTS = 3; // Número máximo de intentos de descarga

    public static void main(String[] args) {
        // Instancias de las implementaciones concretas
        BookDownloader downloader = new GutenbergBookDownloader(SAVE_DIR);
        MetadataExtractor metadataExtractor = new GutenbergMetadataExtractor();
        BookProcessor bookProcessor = new GutenbergBookProcessor();
        MetadataWriter metadataWriter = new CSVMetadataWriter();
        LastIdManager lastIdManager = new FileLastIdManager();

        // Crear directorio de guardado si no existe
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Leer el último ID descargado desde el archivo, si existe
        int startId = lastIdManager.getLastDownloadedId() + 1;

        // Descargar, procesar y guardar metadatos para cada libro secuencialmente
        for (int i = startId; i < startId + NUM_BOOKS; i++) {
            boolean downloadSuccess = false;
            int attempts = 0;

            while (!downloadSuccess && attempts < MAX_DOWNLOAD_ATTEMPTS) {
                try {
                    //System.out.println("Intentando descargar el libro con ID: " + i + " (Intento " + (attempts + 1) + ")");
                    downloader.downloadBook(i);
                    downloadSuccess = true;

                    // Verificar que el archivo realmente se descargó
                    Path bookPath = Path.of(SAVE_DIR, i + ".txt");
                    if (!bookPath.toFile().exists()) {
                        throw new IOException("Archivo no encontrado después de la descarga: " + bookPath);
                    }

                    // Obtener y guardar metadatos
                    Map<String, String> metadata = metadataExtractor.extractMetadata(i);
                    if (metadata != null) {
                        metadataWriter.writeMetadata(metadata);
                    }

                    // Procesar el libro
                    bookProcessor.processBook(i);

                } catch (IOException e) {
                    System.out.println("Error al descargar o procesar el libro con ID: " + i + " en el intento " + (attempts + 1) + ". Error: " + e.getMessage());
                    attempts++;
                    if (attempts >= MAX_DOWNLOAD_ATTEMPTS) {
                        System.out.println("Descarga fallida para el libro con ID: " + i + " después de " + MAX_DOWNLOAD_ATTEMPTS + " intentos.");
                    }
                }
            }
        }

        // Actualizar el archivo con el último ID descargado
        lastIdManager.updateLastDownloadedId(startId + NUM_BOOKS - 1);
    }
}
