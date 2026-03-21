package com.proyectojpa.demo.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Persistencia y borrado de archivos subidos (QR de medios de pago).
 */
@Service
public class FileStorageService {

    private static final String PREFIX_PUBLIC = "/files/medios-pago/";

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    /** Guarda la imagen y devuelve la ruta pública (p. ej. {@code /files/medios-pago/uuid.png}). */
    public String guardarImagenMedioPago(MultipartFile imagenArchivo) throws IOException {
        if (imagenArchivo == null || imagenArchivo.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }
        String original = imagenArchivo.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String fn = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, "medios-pago").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path dest = dir.resolve(fn).normalize();
        if (!dest.startsWith(dir)) {
            throw new IllegalStateException("Ruta de archivo inválida");
        }
        imagenArchivo.transferTo(dest.toFile());
        return PREFIX_PUBLIC + fn;
    }

    /**
     * Si {@code rutaPublica} apunta a un fichero bajo {@code uploads/medios-pago/}, lo elimina del disco.
     */
    public void eliminarSiRutaArchivoLocal(String rutaPublica) {
        if (rutaPublica == null || !rutaPublica.startsWith(PREFIX_PUBLIC)) {
            return;
        }
        String filename = rutaPublica.substring(PREFIX_PUBLIC.length());
        if (filename.isBlank() || filename.contains("..") || filename.indexOf('/') >= 0
                || filename.indexOf('\\') >= 0) {
            return;
        }
        Path base = Paths.get(uploadDir, "medios-pago").toAbsolutePath().normalize();
        Path target = base.resolve(filename).normalize();
        if (!target.startsWith(base)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo eliminar el archivo anterior", e);
        }
    }
}
