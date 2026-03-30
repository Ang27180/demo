package com.proyectojpa.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.ReciboService;
import com.proyectojpa.demo.repository.MedioPagoRepository;
import com.proyectojpa.demo.repository.ReciboRepository;

@Controller
@RequestMapping("/admin/recibos")
public class AdminReciboController {

    private final ReciboRepository reciboRepository;
    private final ReciboService reciboService;
    private final MedioPagoRepository medioPagoRepository;

    public AdminReciboController(ReciboRepository reciboRepository, ReciboService reciboService,
            MedioPagoRepository medioPagoRepository) {
        this.reciboRepository = reciboRepository;
        this.reciboService = reciboService;
        this.medioPagoRepository = medioPagoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("recibos", reciboRepository.findAllWithDetalle());
        return "admin/recibos";
    }

    @GetMapping("/generar")
    public String formularioGenerar(Model model) {
        model.addAttribute("inscripciones", reciboService.listarInscripcionesPendientesPagoSinRecibo());
        model.addAttribute("medios", medioPagoRepository.findAllWithAdmin());
        return "admin/recibos-generar";
    }

    @PostMapping("/generar")
    public String generar(@RequestParam Integer idInscripcion, @RequestParam Integer idMedioPago,
            RedirectAttributes redirectAttributes) {
        try {
            reciboService.generarReciboComoAdministrador(idInscripcion, idMedioPago);
            redirectAttributes.addFlashAttribute("msgAdmin", "Recibo generado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorAdmin", e.getMessage());
        }
        return "redirect:/admin/recibos";
    }

    @PostMapping("/{id}/marcar-pagado")
    public String marcarPagado(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            reciboService.marcarReciboPagadoYActivarInscripcion(id);
            redirectAttributes.addFlashAttribute("msgAdmin", "Recibo marcado como pagado e inscripción activada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorAdmin", e.getMessage());
        }
        return "redirect:/admin/recibos";
    }
}
