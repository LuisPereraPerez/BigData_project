package org.example.implementations;

import org.example.interfaces.MetadataWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class CSVMetadataWriter implements MetadataWriter {
    private static final String METADATA_CSV_FILE = "datalake/metadata.csv";

    @Override
    public void writeMetadata(Map<String, String> metadata) {
        File file = new File(METADATA_CSV_FILE);
        boolean isNewFile = !file.exists();  // Si el archivo no existe, se considera nuevo

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            // Escribir la cabecera si el archivo es nuevo
            if (isNewFile) {
                String header = "ID,Title,Author,Release Date,Most Recently Updated,Language";
                writer.write(header);
                writer.newLine();
            }

            // Escribir la línea de datos
            String csvLine = String.join(",",
                    metadata.get("ID"),
                    metadata.get("Title"),
                    metadata.get("Author"),
                    metadata.get("Release Date"),
                    metadata.get("Most Recently Updated"),
                    metadata.get("Language"));
            writer.write(csvLine);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error al escribir los metadatos en el archivo CSV.");
        }
    }
}
