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
    private final InscripcionRepository inscripcionRepository;

    public ReporteController(ReporteJasperService reporteJasperService,
                             PersonaRepository personaRepository,
                             JdbcTemplate jdbcTemplate,
                             InscripcionRepository inscripcionRepository) {
        this.reporteJasperService = reporteJasperService;
        this.personaRepository = personaRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.inscripcionRepository = inscripcionRepository;
    }

    // -------------------------------
    // 1. Vista Thymeleaf (Estadística General)
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
    // 2. Generar Reporte Estadístico PDF
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
            throw new RuntimeException("Error generando PDF estadístico: " + e.getMessage(), e);
        }
    }

    // -------------------------------
    // 3. Descargar Certificado PDF (Seguro)
    // -------------------------------
    @GetMapping("/certificado/pdf/{idInscripcion}")
    public void descargarCertificado(
            @PathVariable("idInscripcion") Integer idInscripcion,
            HttpServletResponse response) {
        try {
            System.out.println("[CERTIFICADO] Solicitud para inscripción ID: " + idInscripcion);

            Persona personaActual = getPersonaActual();
            if (personaActual == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Debes iniciar sesión para descargar el certificado.");
                return;
            }

            Inscripcion inscripcion = inscripcionRepository.findById(idInscripcion)
                    .orElse(null);
            if (inscripcion == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Inscripción no encontrada.");
                return;
            }

            // Seguridad: Solo el dueño o el ADMIN
            boolean esAdmin = personaActual.getRolId() != null && personaActual.getRolId().equals(1);
            boolean esPropietario = inscripcion.getEstudiante() != null
                    && inscripcion.getEstudiante().getPersona() != null
                    && inscripcion.getEstudiante().getPersona().getId().equals(personaActual.getId());

            if (!esAdmin && !esPropietario) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No tienes permiso para descargar este certificado.");
                return;
            }

            // Autorización facultativa (Acudiente para menores)
            if (esPropietario && "TI".equalsIgnoreCase(personaActual.getTipoDocumento())) {
                Boolean estaAutorizado = inscripcion.getCertificadoAutorizado();
                if (estaAutorizado == null || !estaAutorizado) {
                    response.setContentType("text/html;charset=UTF-8");
                    response.getOutputStream().write("<script>alert('¡ATENCIÓN! Tu acudiente debe autorizar la emisión de firma antes de descargar el certificado.'); window.history.back();</script>".getBytes("UTF-8"));
                    return;
                }
            }

            // Obtención de datos reales para el certificado
            // Usamos nombres de columnas compatibles con el dump
            String sql = "SELECT p.nombre_persona AS nombre_estudiante, " +
                         "       c.nombre         AS nombre_curso, " +
                         "       i.Fecha_Inscripcion " +
                         "FROM inscripcion i " +
                         "JOIN estudiante e ON i.id_estudiante = e.id_estudiante " +
                         "JOIN persona p   ON e.persona_id_persona = p.id_persona " +
                         "JOIN curso c     ON i.id_curso = c.id_curso " +
                         "WHERE i.id_inscripcion = ?";

            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql, idInscripcion);

            if (resultados.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "No se encontraron datos de inscripción.");
                return;
            }

            Map<String, Object> resultado = resultados.get(0);
            Map<String, Object> parametros = new HashMap<>();
            
            parametros.put("NOMBRE_ESTUDIANTE", resultado.get("nombre_estudiante"));
            parametros.put("NOMBRE_CURSO", resultado.get("nombre_curso"));
            parametros.put("FECHA", resultado.get("Fecha_Inscripcion") != null ? resultado.get("Fecha_Inscripcion").toString() : "N/A");

            // --- PROCESAMIENTO DE MARCA DE AGUA (Logo con transparencia) ---
            try {
                java.io.InputStream logoStream = null;
                // PRIORIDAD: logo-sabor.jpg (el que nos pediste)
                try { logoStream = new ClassPathResource("static/imagenes/logo-sabor.jpg").getInputStream(); } 
                catch (Exception e) { 
                    try { logoStream = new ClassPathResource("static/imagenes/logo.jpg").getInputStream(); }
                    catch (Exception e2) { System.err.println("[JASPER] No se encontró logo-sabor.jpg"); }
                }

                if (logoStream != null) {
                    java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(logoStream);
                    if (originalImage != null) {
                        int w = originalImage.getWidth();
                        int h = originalImage.getHeight();
                        java.awt.image.BufferedImage transparentImage = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                        
                        for (int y = 0; y < h; y++) {
                            for (int x = 0; x < w; x++) {
                                int rgba = originalImage.getRGB(x, y);
                                int r = (rgba >> 16) & 0xFF;
                                int g = (rgba >> 8) & 0xFF;
                                int b = rgba & 0xFF;
                                int gray = (r + g + b) / 3;

                                // Si no es blanco (fondo), lo aclaramos y aplicamos transparencia
                                if (gray < 245) {
                                    int alpha = 20; // Marca de agua ultra-suave (aprox 8%)
                                    // Aclaramos el color original para que el negro no sea tan fuerte
                                    int newR = Math.min(255, r + 50);
                                    int newG = Math.min(255, g + 50);
                                    int newB = Math.min(255, b + 50);
                                    int newPixel = (alpha << 24) | (newR << 16) | (newG << 8) | newB;
                                    transparentImage.setRGB(x, y, newPixel);
                                } else {
                                    transparentImage.setRGB(x, y, 0x00000000); // Transparente
                                }
                            }
                        }

                        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                        javax.imageio.ImageIO.write(transparentImage, "png", baos);
                        parametros.put("LOGO_PATH", new java.io.ByteArrayInputStream(baos.toByteArray()));
                    }
                }
            } catch (Exception e) {
                System.err.println("[WARN] Error procesando logo: " + e.getMessage());
                parametros.put("LOGO_PATH", null);
            }

            // --- PROCESAMIENTO DE FIRMA (Transparente y Auto-Crop) ---
            try {
                java.io.InputStream fStream = null;
                try { fStream = new ClassPathResource("static/imagenes/FIRMA.jpg").getInputStream(); } 
                catch (Exception e) { fStream = new ClassPathResource("static/imagenes/firma.png").getInputStream(); }

                if (fStream != null) {
                    java.awt.image.BufferedImage originalFirma = javax.imageio.ImageIO.read(fStream);
                    if (originalFirma != null) {
                        java.awt.image.BufferedImage processFirma = new java.awt.image.BufferedImage(
                                originalFirma.getWidth(), originalFirma.getHeight(), java.awt.image.BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < originalFirma.getHeight(); y++) {
                            for (int x = 0; x < originalFirma.getWidth(); x++) {
                                int rgba = originalFirma.getRGB(x, y);
                                int gray = (((rgba >> 16) & 0xFF) + ((rgba >> 8) & 0xFF) + (rgba & 0xFF)) / 3;
                                if (gray > 180) processFirma.setRGB(x, y, 0x00000000);
                                else processFirma.setRGB(x, y, rgba);
                            }
                        }
                        java.io.ByteArrayOutputStream fBaos = new java.io.ByteArrayOutputStream();
                        javax.imageio.ImageIO.write(processFirma, "png", fBaos);
                        parametros.put("FIRMA_PATH", new java.io.ByteArrayInputStream(fBaos.toByteArray()));
                    }
                }
            } catch (Exception e) {
                parametros.put("FIRMA_PATH", null);
            }

            byte[] pdfBytes = reporteJasperService.generarCertificadoPdf(parametros);
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Certificado.pdf");
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al generar el certificado PDF: " + e.getMessage(), e);
        }
    }

    private Persona getPersonaActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return user.getPersona();
        }
        return null;
    }
}
