package com.proyectojpa.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.proyectojpa.demo.dto.DatoEstadisticoDTO;
import com.proyectojpa.demo.Service.ReporteJasperService;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.security.CustomUserDetails;
import org.springframework.core.io.ClassPathResource;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteJasperService reporteJasperService;
    private final PersonaRepository personaRepository;
    private final JdbcTemplate jdbcTemplate;
    // CAMBIO B: Repositorio de inscripciones para validar seguridad
    private final InscripcionRepository inscripcionRepository;

    public ReporteController(ReporteJasperService reporteJasperService,
                             PersonaRepository personaRepository,
                             JdbcTemplate jdbcTemplate,
                             InscripcionRepository inscripcionRepository) {
        this.reporteJasperService = reporteJasperService;
        this.personaRepository = personaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.inscripcionRepository = inscripcionRepository; // CAMBIO B
    }

    // -------------------------------
    // 1. Vista Thymeleaf
    // -------------------------------
    @GetMapping
    public String mostrarVistaReporte(Model model) {

        String sql = "SELECT genero AS label, COUNT(*) AS valor FROM persona GROUP BY genero";

        List<DatoEstadisticoDTO> datos = jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new DatoEstadisticoDTO(
                        rs.getString("label"),
                        rs.getDouble("valor")
                )
        );

        model.addAttribute("datos", datos);

        return "vistaReporteEstadistico";
    }

    // -------------------------------
    // 2. Generar PDF directamente
    // -------------------------------
    @GetMapping("/estadistico/pdf")
    public void generarReporteEstadistico(HttpServletResponse response) {
        try {

            String sql = "SELECT genero AS label, COUNT(*) AS valor FROM persona GROUP BY genero";

            List<DatoEstadisticoDTO> datos = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> new DatoEstadisticoDTO(
                            rs.getString("label"),
                            rs.getDouble("valor")
                    )
            );

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("TITULO", "Reporte estadístico de personas");
            parametros.put("NUMEROPERSONAS", "Total Personas registradas: " + datos.stream().mapToDouble(DatoEstadisticoDTO::getValor).sum());

            byte[] pdfBytes = reporteJasperService.generarReporteEstadisticoPdf(datos, parametros);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=reporte_estadistico.pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    } // AJUSTE: Se agregó la llave de cierre para el método generarReporteEstadistico

// --- CAMBIO B + E: DESCARGAR CERTIFICADO EN PDF CON VALIDACIÓN DE SEGURIDAD ---
    @GetMapping("/certificado/pdf/{idInscripcion}")
    public void descargarCertificado(
            @PathVariable("idInscripcion") Integer idInscripcion,
            HttpServletResponse response) {
        try {
            System.out.println("[CERTIFICADO] Solicitud recibida para inscripción ID: " + idInscripcion);

            // CAMBIO B: Validación de seguridad — solo el estudiante dueño o un ADMIN puede descargar
            Persona personaActual = getPersonaActual();
            if (personaActual == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión para descargar el certificado.");
                return;
            }

            // Verificar que la inscripción existe
            Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                    .orElse(null);
            if (inscripcion == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Inscripción no encontrada.");
                return;
            }

            // CAMBIO B: Verificar que la persona actual es el estudiante de esta inscripción
            //           o que es ADMIN (rol 1). Los admins pueden ver cualquier certificado.
            boolean esAdmin = personaActual.getRolId() != null && personaActual.getRolId().equals(1);
            boolean esPropietario = inscripcion.getEstudiante() != null
                    && inscripcion.getEstudiante().getPersona() != null
                    && inscripcion.getEstudiante().getPersona().getId().equals(personaActual.getId());

            if (!esAdmin && !esPropietario) {
                System.err.println("[CERTIFICADO] Acceso denegado: persona " + personaActual.getId()
                        + " intentó descargar certificado de inscripción " + idInscripcion);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para descargar este certificado.");
                return;
            }

            // CAMBIO E: SQL corregido con los nombres exactos de columnas del modelo
            // persona.nombre_persona, curso.Nombre (mayúscula), estudiante.persona_id_persona
            String sql = "SELECT p.nombre_persona AS nombre_estudiante, " +
                         "       c.Nombre         AS nombre_curso, " +
                         "       i.fecha_inscripcion " +
                         "FROM inscripcion i " +
                         "JOIN estudiante e ON i.id_estudiante = e.id_estudiante " +
                         "JOIN persona p   ON e.persona_id_persona = p.id_persona " +
                         "JOIN curso c     ON i.id_curso = c.id_curso " +
                         "WHERE i.id_inscripcion = ?";

            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql, idInscripcion);

            if (resultados.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "No se encontraron datos para la inscripción ID: " + idInscripcion);
                return;
            }

            Map<String, Object> resultado = resultados.get(0);

            // Preparamos los parámetros para el template Jasper
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("NOMBRE_ESTUDIANTE",
                    resultado.get("nombre_estudiante") != null ? resultado.get("nombre_estudiante").toString() : "Estudiante");
            parametros.put("NOMBRE_CURSO",
                    resultado.get("nombre_curso") != null ? resultado.get("nombre_curso").toString() : "Curso");

            Object fecha = resultado.get("fecha_inscripcion");
            parametros.put("FECHA", fecha != null ? fecha.toString() : "Fecha no disponible");

            // AJUSTE: Procesamiento inteligente de la imagen para crear una marca de agua "blanca" limpia.
            // Esto analiza cualquier imagen, quita el color de fondo ("recuadro") volviéndolo 100% transparente,
            // y pinta el dibujo real de la marca de agua en gris ultra-suave.
            try {
                java.io.InputStream logoStream = new ClassPathResource("static/imagenes/logo-sabor1.jpg").getInputStream();
                java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(logoStream);
                
                if (originalImage != null) {
                    java.awt.image.BufferedImage transparentImage = new java.awt.image.BufferedImage(
                            originalImage.getWidth(), originalImage.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    
                    // Detectar si el fondo de la imagen es oscuro (negro) o claro (blanco) leyendo el pixel 0,0
                    int bgRgba = originalImage.getRGB(0, 0);
                    int bgGray = (((bgRgba >> 16) & 0xFF) + ((bgRgba >> 8) & 0xFF) + (bgRgba & 0xFF)) / 3;
                    boolean isDarkBackground = (bgGray < 128);
                    
                    int minCropX = originalImage.getWidth();
                    int minCropY = originalImage.getHeight();
                    int maxCropX = 0;
                    int maxCropY = 0;
                    
                    for (int y = 0; y < originalImage.getHeight(); y++) {
                        for (int x = 0; x < originalImage.getWidth(); x++) {
                            int rgba = originalImage.getRGB(x, y);
                            int pixelGray = (((rgba >> 16) & 0xFF) + ((rgba >> 8) & 0xFF) + (rgba & 0xFF)) / 3;
                            
                            // Si el fondo es negro, las partes brillantes (>128) son el logo.
                            // Si el fondo es blanco, las partes oscuras (<128) son el logo.
                            boolean isLogoPixel = isDarkBackground ? (pixelGray > 128) : (pixelGray < 128);
                            
                            if (isLogoPixel) {
                                // Es una línea del logo: pintar de gris suave (blanco perlado) casi translúcido (alpha = 60)
                                int alpha = 60; // Nivel de opacidad (0=invisible, 255=sólido)
                                int rgbAclarado = 200; // Un gris casi blanco para que no moleste a la vista
                                int newPixel = (alpha << 24) | (rgbAclarado << 16) | (rgbAclarado << 8) | rgbAclarado;
                                transparentImage.setRGB(x, y, newPixel);
                                
                                // Actualizar límites del logo real para el Recorte ("Crop") y quitar márgenes inservibles
                                if (x < minCropX) minCropX = x;
                                if (x > maxCropX) maxCropX = x;
                                if (y < minCropY) minCropY = y;
                                if (y > maxCropY) maxCropY = y;
                            } else {
                                // Es parte del fondo inútil (cuadro blanco o negro) -> quitarlo (100% transparente)
                                transparentImage.setRGB(x, y, 0x00000000);
                            }
                        }
                    }
                    
                    // Recortar la imagen resultante (eliminar márgenes vacíos inmensos) para que el logo quede al borde
                    java.awt.image.BufferedImage croppedImage = transparentImage;
                    if (maxCropX > minCropX && maxCropY > minCropY) {
                        int padding = 5;
                        minCropX = Math.max(0, minCropX - padding);
                        minCropY = Math.max(0, minCropY - padding);
                        maxCropX = Math.min(originalImage.getWidth() - 1, maxCropX + padding);
                        maxCropY = Math.min(originalImage.getHeight() - 1, maxCropY + padding);
                        croppedImage = transparentImage.getSubimage(minCropX, minCropY, (maxCropX - minCropX) + 1, (maxCropY - minCropY) + 1);
                    }
                    
                    // Escribir a PNG para preservar transparencia
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(croppedImage, "png", baos);
                    java.io.InputStream transparentLogoStream = new java.io.ByteArrayInputStream(baos.toByteArray());
                    
                    parametros.put("LOGO_PATH", transparentLogoStream);
                } else {
                    parametros.put("LOGO_PATH", null);
                }
            } catch (Exception e) {
                // AJUSTE: Si falla la creación de la marca, enviar null para no estropear la descarga
                parametros.put("LOGO_PATH", null);
                System.err.println("[CERTIFICADO] Error cargando o procesando la marca de agua: " + e.getMessage());
                e.printStackTrace();
            }

            // AJUSTE: Procesamiento inteligente de la firma proporcionada.
            // 1) Carga firma.jpg o firma.png  2) Vuelve transparente el fondo blanco.
            // 3) Recorta los inmensos márgenes para lograr el auto-encuadre perfecto sobre la línea.
            try {
                java.io.InputStream firmaStream = null;
                try {
                    firmaStream = new ClassPathResource("static/imagenes/FIRMA.jpg").getInputStream();
                } catch (Exception e1) {
                    try {
                        firmaStream = new ClassPathResource("static/imagenes/firma.jpg").getInputStream();
                    } catch (Exception e2) {
                        try {
                            firmaStream = new ClassPathResource("static/imagenes/firma.png").getInputStream();
                        } catch (Exception e3) {}
                    }
                }

                if (firmaStream != null) {
                    java.awt.image.BufferedImage originalFirma = javax.imageio.ImageIO.read(firmaStream);
                    if (originalFirma != null) {
                        java.awt.image.BufferedImage processFirma = new java.awt.image.BufferedImage(
                                originalFirma.getWidth(), originalFirma.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                        
                        // Variables para el Auto-Layout (Recorte / Bounding Box)
                        int fMinX = originalFirma.getWidth();
                        int fMinY = originalFirma.getHeight();
                        int fMaxX = 0;
                        int fMaxY = 0;

                        // Escanear cada píxel de la firma
                        for (int y = 0; y < originalFirma.getHeight(); y++) {
                            for (int x = 0; x < originalFirma.getWidth(); x++) {
                                int rgba = originalFirma.getRGB(x, y);
                                int gray = (((rgba >> 16) & 0xFF) + ((rgba >> 8) & 0xFF) + (rgba & 0xFF)) / 3;

                                // Si el color es claro (blanco, casi blanco, grisáceo muy claro del papel)
                                if (gray > 200) {
                                    // Poner ese píxel 100% invisible (Alpha = 0)
                                    processFirma.setRGB(x, y, 0x00000000);
                                } else {
                                    // Es "Tinta" de la pluma. Pintamos el negro original opaco.
                                    processFirma.setRGB(x, y, rgba);
                                    
                                    // A su vez calculamos la caja real (quitando márgenes inmensos)
                                    if (x < fMinX) fMinX = x;
                                    if (x > fMaxX) fMaxX = x;
                                    if (y < fMinY) fMinY = y;
                                    if (y > fMaxY) fMaxY = y;
                                }
                            }
                        }

                        // Realizar el recorte mágico (Auto Crop)
                        java.awt.image.BufferedImage finalFirma = processFirma;
                        if (fMaxX > fMinX && fMaxY > fMinY) {
                            int pad = 5; // Píxeles de respiro alrededor de la firma recortada
                            fMinX = Math.max(0, fMinX - pad);
                            fMinY = Math.max(0, fMinY - pad);
                            fMaxX = Math.min(originalFirma.getWidth() - 1, fMaxX + pad);
                            fMaxY = Math.min(originalFirma.getHeight() - 1, fMaxY + pad);
                            finalFirma = processFirma.getSubimage(fMinX, fMinY, (fMaxX - fMinX) + 1, (fMaxY - fMinY) + 1);
                        }

                        // Convertir la imagen procesada en flujo PNG compatible con el certificado 
                        java.io.ByteArrayOutputStream firmasBaos = new java.io.ByteArrayOutputStream();
                        javax.imageio.ImageIO.write(finalFirma, "png", firmasBaos);
                        java.io.InputStream finalFirmaStream = new java.io.ByteArrayInputStream(firmasBaos.toByteArray());
                        
                        parametros.put("FIRMA_PATH", finalFirmaStream);
                    } else {
                        parametros.put("FIRMA_PATH", null);
                    }
                } else {
                     parametros.put("FIRMA_PATH", null);
                }
            } catch (Exception e) {
                parametros.put("FIRMA_PATH", null);
                System.err.println("[CERTIFICADO] Advertencia: Hubo un problema procesando la firma mágica: " + e.getMessage());
            }

            // Generamos el PDF
            byte[] pdfBytes = reporteJasperService.generarCertificadoPdf(parametros);

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new RuntimeException("El PDF generado está vacío. Verifica el template Certificado.jrxml.");
            }

            // Nombre de archivo seguro (sin caracteres especiales)
            String nombreCurso = resultado.get("nombre_curso") != null
                    ? resultado.get("nombre_curso").toString().replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ ]", "").replace(" ", "_")
                    : "Curso";
            String nombreArchivo = "Certificado_" + nombreCurso + ".pdf";

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + nombreArchivo + "\"");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
            response.getOutputStream().close();

            System.out.println("[CERTIFICADO] PDF generado exitosamente: " + nombreArchivo);

        } catch (Exception e) {
            System.err.println("[CERTIFICADO] Error fatal: " + e.getMessage());
            e.printStackTrace();
            try {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Error al generar el certificado: " + e.getMessage());
                }
            } catch (Exception ex) {
                // Ignore — la respuesta ya fue enviada
            }
        }
    }

    // CAMBIO B: Helper para obtener la persona del contexto de seguridad de Spring
    private Persona getPersonaActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return user.getPersona();
        }
        return null;
    }
}
