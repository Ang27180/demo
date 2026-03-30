package com.proyectojpa.demo.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Sirve PDFs de lecciones con cabeceras adecuadas para iframe / embed (evita fallos con ResourceHandler + file: en Windows).
 */
@Controller
public class LeccionPdfController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @GetMapping("/files/lecciones-pdf/{filename:.+}")
    public ResponseEntity<Resource> servirPdf(@PathVariable String filename) {
        if (filename == null || filename.isBlank() || filename.contains("..")
                || filename.indexOf('/') >= 0 || filename.indexOf('\\') >= 0) {
            return ResponseEntity.notFound().build();
        }
        Path base = Paths.get(uploadDir, "lecciones-pdf").toAbsolutePath().normalize();
        Path file = base.resolve(filename).normalize();
        if (!file.startsWith(base) || !Files.isRegularFile(file) || !Files.isReadable(file)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(file);
        String safeName = filename.replace("\"", "");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeName + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600")
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
