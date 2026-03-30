package com.proyectojpa.demo.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Set;
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
    private static final String PREFIX_TUTOR = "/files/tutores/";
    private static final String PREFIX_LECCION_PDF = "/files/lecciones-pdf/";
    private static final Set<String> EXT_IMAGEN = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

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

    /** Guarda la foto del tutor y devuelve la ruta pública (p. ej. {@code /files/tutores/uuid.jpg}). */
    public String guardarFotoTutor(MultipartFile imagenArchivo) throws IOException {
        if (imagenArchivo == null || imagenArchivo.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }
        String contentType = imagenArchivo.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe ser una imagen");
        }
        String original = imagenArchivo.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.')).toLowerCase(Locale.ROOT);
        }
        if (!EXT_IMAGEN.contains(ext)) {
            ext = ".jpg";
        }
        String fn = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, "tutores").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path dest = dir.resolve(fn).normalize();
        if (!dest.startsWith(dir)) {
            throw new IllegalStateException("Ruta de archivo inválida");
        }
        imagenArchivo.transferTo(dest.toFile());
        return PREFIX_TUTOR + fn;
    }

    /** Si {@code rutaPublica} apunta a un fichero bajo {@code uploads/tutores/}, lo elimina del disco. */
    public void eliminarSiRutaFotoTutor(String rutaPublica) {
        if (rutaPublica == null || !rutaPublica.startsWith(PREFIX_TUTOR)) {
            return;
        }
        String filename = rutaPublica.substring(PREFIX_TUTOR.length());
        if (filename.isBlank() || filename.contains("..") || filename.indexOf('/') >= 0
                || filename.indexOf('\\') >= 0) {
            return;
        }
        Path base = Paths.get(uploadDir, "tutores").toAbsolutePath().normalize();
        Path target = base.resolve(filename).normalize();
        if (!target.startsWith(base)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo eliminar la foto anterior del tutor", e);
        }
    }

    /** Guarda un PDF de lección y devuelve la ruta pública (p. ej. {@code /files/lecciones-pdf/uuid.pdf}). */
    public String guardarPdfLeccion(MultipartFile archivo) throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Archivo vacío");
        }
        String original = archivo.getOriginalFilename();
        String ct = archivo.getContentType();
        boolean okPdf = original != null && original.toLowerCase(Locale.ROOT).endsWith(".pdf");
        if (ct != null && ct.toLowerCase(Locale.ROOT).contains("pdf")) {
            okPdf = true;
        }
        if (!okPdf) {
            throw new IllegalArgumentException("El archivo debe ser un PDF (.pdf)");
        }
        String ext = ".pdf";
        String fn = UUID.randomUUID() + ext;
        Path dir = Paths.get(uploadDir, "lecciones-pdf").toAbsolutePath().normalize();
        Files.createDirectories(dir);
        Path dest = dir.resolve(fn).normalize();
        if (!dest.startsWith(dir)) {
            throw new IllegalStateException("Ruta de archivo inválida");
        }
        archivo.transferTo(dest.toFile());
        return PREFIX_LECCION_PDF + fn;
    }

    public void eliminarSiRutaPdfLeccion(String rutaPublica) {
        if (rutaPublica == null || !rutaPublica.startsWith(PREFIX_LECCION_PDF)) {
            return;
        }
        String filename = rutaPublica.substring(PREFIX_LECCION_PDF.length());
        if (filename.isBlank() || filename.contains("..") || filename.indexOf('/') >= 0
                || filename.indexOf('\\') >= 0) {
            return;
        }
        Path base = Paths.get(uploadDir, "lecciones-pdf").toAbsolutePath().normalize();
        Path target = base.resolve(filename).normalize();
        if (!target.startsWith(base)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo eliminar el PDF de la lección", e);
        }
    }
}
