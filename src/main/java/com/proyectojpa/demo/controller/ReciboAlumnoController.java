package com.proyectojpa.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.ReciboService;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.MedioPagoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/mis-cursos/recibo")
public class ReciboAlumnoController {

    private final InscripcionRepository inscripcionRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final ReciboService reciboService;

    public ReciboAlumnoController(InscripcionRepository inscripcionRepository,
            MedioPagoRepository medioPagoRepository,
            ReciboService reciboService) {
        this.inscripcionRepository = inscripcionRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.reciboService = reciboService;
    }

    @GetMapping("/nuevo/{idInscripcion}")
    public String formularioNuevo(@PathVariable Integer idInscripcion, Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Inscripcion insc = inscripcionRepository.findByIdWithEstudiantePersonaAndCurso(idInscripcion).orElseThrow();
        if (!insc.getEstudiante().getPersona().getId().equals(userDetails.getPersona().getId())) {
            return "redirect:/mis-cursos?error=recibo";
        }
        model.addAttribute("inscripcion", insc);
        model.addAttribute("medios", medioPagoRepository.findAllWithAdmin());
        return "recibo-nuevo";
    }

    @PostMapping("/generar")
    public String generar(@RequestParam Integer idInscripcion, @RequestParam Integer idMedioPago,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            Recibo r = reciboService.generarRecibo(userDetails.getPersona(), idInscripcion, idMedioPago);
            redirectAttributes.addFlashAttribute("msgRecibo", "Recibo generado. Código: " + r.getCodigoQrUnico());
            return "redirect:/recibos/" + r.getId() + "/ver";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorRecibo", e.getMessage());
            return "redirect:/mis-cursos/recibo/nuevo/" + idInscripcion;
        }
    }
}
