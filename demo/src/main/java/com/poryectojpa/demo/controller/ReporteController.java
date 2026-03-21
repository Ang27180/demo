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
            @PathVariable Integer idInscripcion, 
            HttpServletResponse response) {
        try {
            // 1. Buscamos la inscripción en la base de datos
            // Usamos una consulta SQL personalizada para obtener los datos necesarios
            String sql = "SELECT p.nombre_persona, c.Nombre, i.fecha_inscripcion " +
                         "FROM inscripcion i " +
                         "JOIN estudiante e ON i.id_estudiante = e.id_estudiante " +
                         "JOIN persona p ON e.persona_id_persona = p.id_persona " +
                         "JOIN curso c ON i.id_curso = c.id_curso " +
                         "WHERE i.id_inscripcion = ?";

            Map<String, Object> resultado = jdbcTemplate.queryForMap(sql, idInscripcion);

            // 2. Preparamos los parámetros que se enviarán a la plantilla Certificado.jrxml
            Map<String, Object> parametros = new HashMap<>();
            parametros.put("NOMBRE_ESTUDIANTE", resultado.get("nombre_persona"));
            parametros.put("NOMBRE_CURSO", resultado.get("Nombre"));
            parametros.put("FECHA", resultado.get("fecha_inscripcion").toString());

            // 3. Generamos el arreglo de bytes del PDF usando el servicio de Jasper
            byte[] pdfBytes = reporteJasperService.generarCertificadoPdf(parametros);

            // 4. Configuramos la respuesta HTTP para que el navegador lo descargue como PDF
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=Certificado_" + resultado.get("Nombre") + ".pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando el certificado PDF: " + e.getMessage());
        }
    }
}
