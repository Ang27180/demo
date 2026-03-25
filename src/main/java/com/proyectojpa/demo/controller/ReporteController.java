package com.poryectojpa.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.poryectojpa.demo.dto.DatoEstadisticoDTO;
import com.poryectojpa.demo.Service.ReporteJasperService;
import com.poryectojpa.demo.repository.personaRepository;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable; // NUEVO: Para recibir el ID de la inscripción
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteJasperService reporteJasperService;
    private final personaRepository personaRepository;
    private final JdbcTemplate jdbcTemplate;

    public ReporteController(ReporteJasperService reporteJasperService,
                             personaRepository personaRepository,
                             JdbcTemplate jdbcTemplate) {
        this.reporteJasperService = reporteJasperService;
        this.personaRepository = personaRepository;
        this.jdbcTemplate = jdbcTemplate;
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

// --- NUEVA FUNCIONALIDAD: DESCARGAR CERTIFICADO EN PDF ---
    @GetMapping("/certificado/pdf/{idInscripcion}")
    public void descargarCertificado(
            @PathVariable("idInscripcion") Integer idInscripcion, 
            HttpServletResponse response) {
        try {
            System.out.println("Generando certificado para inscripción ID: " + idInscripcion);
            
            // 1. Buscamos la inscripción en la base de datos
            String sql = "SELECT p.nombre_persona as nombre_estudiante, c.Nombre as nombre_curso, i.fecha_inscripcion " +
                         "FROM inscripcion i " +
                         "JOIN estudiante e ON i.id_estudiante = e.id_estudiante " +
                         "JOIN persona p ON e.persona_id_persona = p.id_persona " +
                         "JOIN curso c ON i.id_curso = c.id_curso " +
                         "WHERE i.id_inscripcion = ?";

            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql, idInscripcion);
            
            if (resultados.isEmpty()) {
                throw new RuntimeException("No se encontró la inscripción con ID: " + idInscripcion);
            }

            Map<String, Object> resultado = resultados.get(0);

            // 2. Preparamos los parámetros para Jasper
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("NOMBRE_ESTUDIANTE", resultado.get("nombre_estudiante"));
            parametros.put("NOMBRE_CURSO", resultado.get("nombre_curso"));
            
            // Formateo de fecha si es necesario (o toString por ahora)
            Object fecha = resultado.get("fecha_inscripcion");
            parametros.put("FECHA", fecha != null ? fecha.toString() : "Fecha no disponible");

            // 3. Generamos el PDF
            byte[] pdfBytes = reporteJasperService.generarCertificadoPdf(parametros);

            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new RuntimeException("El archivo PDF generado está vacío.");
            }

            // 4. Salida
            String nombreArchivo = "Certificado_" + resultado.get("nombre_curso").toString().replace(" ", "_") + ".pdf";
            
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=" + nombreArchivo);
            response.setContentLength(pdfBytes.length);
            
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
            response.getOutputStream().close();
            
            System.out.println("Certificado generado exitosamente: " + nombreArchivo);

        } catch (Exception e) {
            System.err.println("Error fatal al generar certificado: " + e.getMessage());
            e.printStackTrace();
            try {
                if (!response.isCommitted()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar el PDF: " + e.getMessage());
                }
            } catch (Exception ex) {
                // Ignore
            }
        }
    }
}
