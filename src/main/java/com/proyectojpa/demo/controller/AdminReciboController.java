package com.proyectojpa.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.ReciboService;
import com.proyectojpa.demo.repository.ReciboRepository;

@Controller
@RequestMapping("/admin/recibos")
public class AdminReciboController {

    private final ReciboRepository reciboRepository;
    private final ReciboService reciboService;

    public AdminReciboController(ReciboRepository reciboRepository, ReciboService reciboService) {
        this.reciboRepository = reciboRepository;
        this.reciboService = reciboService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("recibos", reciboRepository.findAllWithDetalle());
        return "admin/recibos";
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
