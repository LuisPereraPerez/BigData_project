package org.example.Control;

import org.example.interfaces.LastIdManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileLastIdManager implements LastIdManager {
    // Ruta al archivo dentro del módulo Crawler
    private static final String LAST_ID_FILE = "crawler/last_id.txt";

    @Override
    public int getLastDownloadedId() {
        try {
            Path filePath = Paths.get(LAST_ID_FILE);
            File file = filePath.toFile();

            // Verifica si el archivo no existe y lo crea dentro del módulo Crawler
            if (!file.exists()) {
                Files.createFile(filePath);
                System.out.println("Archivo de ID no encontrado, creado uno nuevo en el módulo Crawler. Comenzando desde 0.");
                return 0; // Retorna 0 al crear un nuevo archivo
            }

            // Si el archivo existe, lee el último ID descargado
            String lastId = new String(Files.readAllBytes(filePath));
            return Integer.parseInt(lastId.trim());

        } catch (IOException e) {
            System.out.println("No se pudo leer el archivo de ID, comenzando desde 0.");
        } catch (NumberFormatException e) {
            System.out.println("El archivo de ID está vacío o tiene un formato incorrecto. Comenzando desde 0.");
        }
        return 0;
    }

    @Override
    public void updateLastDownloadedId(int lastId) {
        try {
            Path filePath = Paths.get(LAST_ID_FILE);
            Files.write(filePath, String.valueOf(lastId).getBytes());
        } catch (IOException e) {
            System.out.println("No se pudo actualizar el archivo de ID.");
        }
    }
}
