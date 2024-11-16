package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FragmentHandler {
    private final Path userTempDir;
    private final Path userDownloadsDir;

    public FragmentHandler(String username) throws IOException {
        // Directorio base del usuario actual
        Path userHome = Paths.get(System.getProperty("user.home"));
        Path descargasHome =  Paths.get(System.getProperty("user.home"), "Downloads");

        // Crear un directorio temporal exclusivo para este usuario
        this.userTempDir = userHome.resolve(username + "_temp");
        Files.createDirectories(userTempDir);

        // Crear un directorio de descargas exclusivo para este usuario
        this.userDownloadsDir = descargasHome.resolve(username + "_files");
        Files.createDirectories(userDownloadsDir);
    }

    public Path getUserTempDir() {
        return userTempDir;
    }

    public Path getUserDownloadsDir() {
        return userDownloadsDir;
    }

    public void guardarFragmento(String filename, int packetNumber, byte[] contenido) throws IOException {
        // Guardar cada fragmento en un archivo individual dentro del directorio temporal
        Path fragmentPath = userTempDir.resolve(filename + "_fragment_" + packetNumber);
        try (FileOutputStream fos = new FileOutputStream(fragmentPath.toFile())) {
            fos.write(contenido);
        }
    }

    public void reconstruirArchivo(String filename, int totalPackets) throws IOException {
        // Ruta del archivo final en el directorio de descargas
        Path finalFilePath = userDownloadsDir.resolve("received_" + filename);

        // Validar existencia de todos los fragmentos
        for (int i = 1; i <= totalPackets; i++) {
            Path fragmentPath = userTempDir.resolve(filename + "_fragment_" + i);
            if (!Files.exists(fragmentPath)) {
                throw new IllegalStateException("Paquete faltante desde el núm # " + i);
            }
        }

        // Leer todos los fragmentos y reconstruir el archivo
        try (FileOutputStream fos = new FileOutputStream(finalFilePath.toFile())) {
            for (int i = 1; i <= totalPackets; i++) {
                Path fragmentPath = userTempDir.resolve(filename + "_fragment_" + i);
                byte[] fragmentContent = Files.readAllBytes(fragmentPath);
                fos.write(fragmentContent);
            }
        }

        System.out.println("Archivo guardado: " + finalFilePath);
    }

    private void eliminarFragmentos(String filename, int totalPackets) throws IOException {
        for (int i = 1; i <= totalPackets; i++) {
            Path fragmentPath = userTempDir.resolve(filename + "_fragment_" + i);
            Files.deleteIfExists(fragmentPath);
        }
    }

    public void eliminarCarpetaTemporal() throws IOException {
        // Eliminar todos los archivos y la carpeta temporal
        Files.walk(userTempDir)
                .map(Path::toFile)
                .forEach(File::delete);
        Files.deleteIfExists(userTempDir);
        System.out.println("Carpeta temporal eliminada: " + userTempDir);
    }
}

