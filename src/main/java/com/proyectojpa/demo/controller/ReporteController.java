package com.proyectojpa.demo.controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import com.proyectojpa.demo.dto.DatoEstadisticoDTO;
import com.proyectojpa.demo.Service.CertificadoAutorizacionService;
import com.proyectojpa.demo.Service.QrCodeService;
import com.proyectojpa.demo.Service.ReciboAutorizacionService;
import com.proyectojpa.demo.Service.ReporteJasperService;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.ReciboRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final ReporteJasperService reporteJasperService;
    private final JdbcTemplate jdbcTemplate;
    private final InscripcionRepository inscripcionRepository;
    private final CertificadoAutorizacionService certificadoAutorizacionService;
    private final ReciboRepository reciboRepository;
    private final ReciboAutorizacionService reciboAutorizacionService;
    private final QrCodeService qrCodeService;

    public ReporteController(ReporteJasperService reporteJasperService,
                             JdbcTemplate jdbcTemplate,
                             InscripcionRepository inscripcionRepository,
                             CertificadoAutorizacionService certificadoAutorizacionService,
                             ReciboRepository reciboRepository,
                             ReciboAutorizacionService reciboAutorizacionService,
                             QrCodeService qrCodeService) {
        this.reporteJasperService = reporteJasperService;
        this.jdbcTemplate = jdbcTemplate;
        this.inscripcionRepository = inscripcionRepository;
        this.certificadoAutorizacionService = certificadoAutorizacionService;
        this.reciboRepository = reciboRepository;
        this.reciboAutorizacionService = reciboAutorizacionService;
        this.qrCodeService = qrCodeService;
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
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) throws java.io.IOException {
        if (userDetails == null || userDetails.getPersona() == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        if (!inscripcionRepository.existsById(idInscripcion)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!certificadoAutorizacionService.puedeDescargar(userDetails.getPersona(), idInscripcion)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

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

    @GetMapping("/recibo/pdf/{idRecibo}")
    public void descargarReciboPdf(@PathVariable Integer idRecibo,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            HttpServletResponse response) throws java.io.IOException {
        if (userDetails == null || userDetails.getPersona() == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        Recibo recibo = reciboRepository.findByIdWithDetalle(idRecibo).orElse(null);
        if (recibo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!reciboAutorizacionService.puedeVerRecibo(userDetails.getPersona(), recibo)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        try {
            byte[] qrBytes = qrCodeService.generarPngBytes(recibo.getCodigoQrUnico());
            BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrBytes));

            Map<String, Object> parametros = new HashMap<>();
            parametros.put("NOMBRE_CURSO", recibo.getInscripcion().getCurso().getNombre());
            parametros.put("NOMBRE_ESTUDIANTE", recibo.getInscripcion().getEstudiante().getPersona().getNombre());
            parametros.put("MEDIO_NOMBRE", recibo.getMedioPago().getNombre());
            parametros.put("MEDIO_TIPO", recibo.getMedioPago().getTipo());
            parametros.put("FECHA_EMISION", recibo.getFechaEmision().toString());
            parametros.put("REFERENCIA", recibo.getCodigoQrUnico());
            parametros.put("ESTADO_RECIBO", recibo.getEstado());
            parametros.put("QR_IMAGE", qrImage);

            byte[] pdfBytes = reporteJasperService.generarReciboPdf(parametros);

            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition",
                    "attachment; filename=Recibo_" + recibo.getId() + ".pdf");
            response.setContentLength(pdfBytes.length);
            response.getOutputStream().write(pdfBytes);
            response.getOutputStream().flush();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando el recibo PDF: " + e.getMessage());
        }
    }
}
