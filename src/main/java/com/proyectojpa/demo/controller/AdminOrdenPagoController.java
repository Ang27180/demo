package com.proyectojpa.demo.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.OrdenPagoService;
import com.proyectojpa.demo.domain.OrdenPagoEstados;
import com.proyectojpa.demo.models.ComprobantePago;
import com.proyectojpa.demo.models.OrdenPago;
import com.proyectojpa.demo.repository.ComprobantePagoRepository;
import com.proyectojpa.demo.repository.HistorialPagoRepository;
import com.proyectojpa.demo.repository.OrdenPagoRepository;

@Controller
@RequestMapping("/admin/pagos")
public class AdminOrdenPagoController {

    private final OrdenPagoRepository ordenPagoRepository;
    private final OrdenPagoService ordenPagoService;
    private final ComprobantePagoRepository comprobantePagoRepository;
    private final HistorialPagoRepository historialPagoRepository;

    public AdminOrdenPagoController(OrdenPagoRepository ordenPagoRepository,
            OrdenPagoService ordenPagoService,
            ComprobantePagoRepository comprobantePagoRepository,
            HistorialPagoRepository historialPagoRepository) {
        this.ordenPagoRepository = ordenPagoRepository;
        this.ordenPagoService = ordenPagoService;
        this.comprobantePagoRepository = comprobantePagoRepository;
        this.historialPagoRepository = historialPagoRepository;
    }

    @GetMapping("/ordenes")
    public String listar(Model model, @RequestParam(name = "estado", required = false) String estado) {
        if (estado != null && !estado.isBlank()) {
            model.addAttribute("ordenes", ordenPagoRepository.findByEstadoInWithDetalleOrderByFechaCreacionDesc(
                    java.util.List.of(estado.trim())));
        } else {
            model.addAttribute("ordenes", ordenPagoRepository.findAllWithDetalleOrderByFechaCreacionDesc());
        }
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("estadosFiltro", java.util.List.of(
                OrdenPagoEstados.PENDIENTE,
                OrdenPagoEstados.COMPROBANTE_CARGADO,
                OrdenPagoEstados.EN_REVISION,
                OrdenPagoEstados.APROBADO,
                OrdenPagoEstados.RECHAZADO,
                OrdenPagoEstados.VENCIDO));
        return "admin/pagos-ordenes";
    }

    @GetMapping("/ordenes/{id}")
    public String detalle(@PathVariable Integer id, Model model) {
        OrdenPago o = ordenPagoService.obtenerOrdenPorIdParaAdmin(id);
        model.addAttribute("orden", o);
        comprobantePagoRepository.findByOrdenPago_Id(id).ifPresent(c -> model.addAttribute("comprobante", c));
        model.addAttribute("historial", historialPagoRepository.findByOrdenPago_IdOrderByFechaAsc(id));
        return "admin/pago-orden-detalle";
    }

    @GetMapping("/ordenes/{id}/comprobante/archivo")
    public ResponseEntity<Resource> archivo(@PathVariable Integer id) throws Exception {
        OrdenPago o = ordenPagoService.obtenerOrdenPorIdParaAdmin(id);
        ComprobantePago c = comprobantePagoRepository.findByOrdenPago_Id(o.getId())
                .orElseThrow(() -> new IllegalArgumentException("Sin comprobante"));
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

    @PostMapping("/ordenes/{id}/revision")
    public String ponerEnRevision(@PathVariable Integer id, RedirectAttributes ra,
            org.springframework.security.core.Authentication auth) {
        try {
            ordenPagoService.marcarEnRevision(id, auth != null ? auth.getName() : "admin");
            ra.addFlashAttribute("msgAdmin", "Orden marcada en revisión.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorAdmin", e.getMessage());
        }
        return "redirect:/admin/pagos/ordenes/" + id;
    }

    @PostMapping("/ordenes/{id}/aprobar")
    public String aprobar(@PathVariable Integer id,
            @RequestParam(value = "detalle", required = false) String detalle,
            RedirectAttributes ra,
            org.springframework.security.core.Authentication auth) {
        try {
            ordenPagoService.aprobar(id, auth != null ? auth.getName() : "admin", detalle);
            ra.addFlashAttribute("msgAdmin", "Pago aprobado e inscripción activada.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorAdmin", e.getMessage());
        }
        return "redirect:/admin/pagos/ordenes/" + id;
    }

    @PostMapping("/ordenes/{id}/rechazar")
    public String rechazar(@PathVariable Integer id,
            @RequestParam("motivo") String motivo,
            RedirectAttributes ra,
            org.springframework.security.core.Authentication auth) {
        try {
            ordenPagoService.rechazar(id, auth != null ? auth.getName() : "admin", motivo);
            ra.addFlashAttribute("msgAdmin", "Orden rechazada.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorAdmin", e.getMessage());
        }
        return "redirect:/admin/pagos/ordenes/" + id;
    }
}
