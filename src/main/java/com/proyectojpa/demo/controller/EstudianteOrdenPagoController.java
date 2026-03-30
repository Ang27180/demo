package com.proyectojpa.demo.controller;

import java.math.BigDecimal;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.OrdenPagoService;
import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.domain.OrdenPagoEstados;
import com.proyectojpa.demo.models.ComprobantePago;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.OrdenPago;
import com.proyectojpa.demo.repository.ComprobantePagoRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/mis-cursos/pagos")
public class EstudianteOrdenPagoController {

    private final InscripcionRepository inscripcionRepository;
    private final OrdenPagoService ordenPagoService;
    private final ComprobantePagoRepository comprobantePagoRepository;

    public EstudianteOrdenPagoController(InscripcionRepository inscripcionRepository,
            OrdenPagoService ordenPagoService,
            ComprobantePagoRepository comprobantePagoRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.ordenPagoService = ordenPagoService;
        this.comprobantePagoRepository = comprobantePagoRepository;
    }

    @GetMapping("/orden/inscripcion/{idInscripcion}")
    public String prepararOrden(@PathVariable Integer idInscripcion, Model model,
            @AuthenticationPrincipal CustomUserDetails user) {
        Inscripcion insc = inscripcionRepository.findByIdWithEstudiantePersonaAndCurso(idInscripcion)
                .orElseThrow();
        if (!insc.getEstudiante().getPersona().getId().equals(user.getPersona().getId())) {
            return "redirect:/mis-cursos?error=pago";
        }
        if (!InscripcionEstados.PENDIENTE_PAGO.equals(insc.getEstado().getCodigo())) {
            return "redirect:/mis-cursos?error=pago";
        }
        OrdenPago activa = ordenPagoService.obtenerOrdenActivaPorInscripcion(idInscripcion);
        if (activa != null) {
            return "redirect:/mis-cursos/pagos/orden/" + activa.getId();
        }
        model.addAttribute("inscripcion", insc);
        return "pago-orden-confirmar";
    }

    @PostMapping("/orden/inscripcion/{idInscripcion}")
    public String crearOrden(@PathVariable Integer idInscripcion,
            @AuthenticationPrincipal CustomUserDetails user,
            RedirectAttributes ra) {
        try {
            OrdenPago o = ordenPagoService.crearOrdenNequi(user.getPersona(), idInscripcion);
            ra.addFlashAttribute("msgPago", "Orden creada. Sigue las instrucciones para pagar por Nequi.");
            return "redirect:/mis-cursos/pagos/orden/" + o.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("errorPago", e.getMessage());
            return "redirect:/mis-cursos/pagos/orden/inscripcion/" + idInscripcion;
        }
    }

    @GetMapping("/orden/{idOrden}")
    public String verOrden(@PathVariable Integer idOrden, Model model,
            @AuthenticationPrincipal CustomUserDetails user) {
        OrdenPago o = ordenPagoService.obtenerOrdenPorIdParaEstudiante(idOrden, user.getPersona());
        model.addAttribute("orden", o);
        model.addAttribute("ordenAbierta", OrdenPagoEstados.esActivo(o.getEstado()));
        comprobantePagoRepository.findByOrdenPago_Id(idOrden).ifPresent(c -> model.addAttribute("comprobante", c));
        return "pago-orden-detalle-estudiante";
    }

    @PostMapping("/orden/{idOrden}/comprobante")
    public String subirComprobante(
            @PathVariable Integer idOrden,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("fechaPago") java.time.LocalDate fechaPago,
            @RequestParam(value = "horaPago", required = false) String horaPago,
            @RequestParam(value = "telefonoPagador", required = false) String telefonoPagador,
            @RequestParam(value = "ultimos4Digitos", required = false) String ultimos4Digitos,
            @RequestParam("valorReportado") BigDecimal valorReportado,
            @RequestParam(value = "observacionEstudiante", required = false) String observacionEstudiante,
            RedirectAttributes ra) {
        try {
            java.time.LocalDateTime fechaHora = OrdenPagoService.combinarFechaHoraPago(fechaPago, horaPago);
            ordenPagoService.guardarComprobante(user.getPersona(), idOrden, archivo, fechaHora, telefonoPagador,
                    ultimos4Digitos, valorReportado, observacionEstudiante);
            ra.addFlashAttribute("msgPago", "Comprobante recibido. El equipo validará tu pago pronto.");
            return "redirect:/mis-cursos/pagos/orden/" + idOrden;
        } catch (Exception e) {
            ra.addFlashAttribute("errorPago", e.getMessage());
            return "redirect:/mis-cursos/pagos/orden/" + idOrden;
        }
    }

    @GetMapping("/orden/{idOrden}/comprobante/archivo")
    public ResponseEntity<Resource> descargarPropio(@PathVariable Integer idOrden,
            @AuthenticationPrincipal CustomUserDetails user) throws Exception {
        OrdenPago o = ordenPagoService.obtenerOrdenPorIdParaEstudiante(idOrden, user.getPersona());
        ComprobantePago c = comprobantePagoRepository.findByOrdenPago_Id(o.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sin comprobante"));
        return construirRespuestaArchivo(c);
    }

    private ResponseEntity<Resource> construirRespuestaArchivo(ComprobantePago c) throws Exception {
        java.nio.file.Path path = ordenPagoService.resolverArchivoComprobante(c);
        if (!java.nio.file.Files.isRegularFile(path)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(path.toFile());
        MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
        if (c.getContentType() != null) {
            try {
                mt = MediaType.parseMediaType(c.getContentType());
            } catch (Exception ignored) {
                // default
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + c.getNombreArchivoOriginal() + "\"")
                .contentType(mt)
                .body(resource);
    }

}
