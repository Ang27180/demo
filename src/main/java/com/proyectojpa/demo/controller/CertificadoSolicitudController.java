package com.proyectojpa.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.CertificadoNotificacionAcudienteService;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/mis-cursos/certificado")
public class CertificadoSolicitudController {

    private final CertificadoNotificacionAcudienteService certificadoNotificacionAcudienteService;

    public CertificadoSolicitudController(CertificadoNotificacionAcudienteService certificadoNotificacionAcudienteService) {
        this.certificadoNotificacionAcudienteService = certificadoNotificacionAcudienteService;
    }

    @PostMapping("/solicitar-autorizacion")
    public String solicitarAutorizacion(@RequestParam Integer idInscripcion,
            @RequestParam(required = false) Integer idCurso,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            certificadoNotificacionAcudienteService.solicitarAutorizacionCertificado(userDetails.getPersona(),
                    idInscripcion);
            redirectAttributes.addFlashAttribute("mensajeProgreso",
                    "Se notificó a tu acudiente por correo. Podrá autorizar el certificado desde su panel.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorProgreso", e.getMessage());
        }
        if (idCurso != null) {
            return "redirect:/cursos/" + idCurso;
        }
        return "redirect:/mis-cursos";
    }
}
