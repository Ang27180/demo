package com.proyectojpa.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.proyectojpa.demo.Service.QrCodeService;
import com.proyectojpa.demo.Service.ReciboAutorizacionService;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.ReciboRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
public class ReciboVistaController {

    private final ReciboRepository reciboRepository;
    private final QrCodeService qrCodeService;
    private final ReciboAutorizacionService reciboAutorizacionService;

    public ReciboVistaController(ReciboRepository reciboRepository, QrCodeService qrCodeService,
            ReciboAutorizacionService reciboAutorizacionService) {
        this.reciboRepository = reciboRepository;
        this.qrCodeService = qrCodeService;
        this.reciboAutorizacionService = reciboAutorizacionService;
    }

    @GetMapping("/recibos/{id}/ver")
    public String ver(@PathVariable Integer id, Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Recibo r = reciboRepository.findByIdWithDetalle(id).orElseThrow();
        if (!reciboAutorizacionService.puedeVerRecibo(userDetails.getPersona(), r)) {
            boolean acudiente = userDetails.getPersona().getRolId() != null
                    && userDetails.getPersona().getRolId() == 4;
            return acudiente ? "redirect:/acudiente/panel?error=recibo" : "redirect:/mis-cursos?error=recibo";
        }
        boolean esAcudiente = userDetails.getPersona().getRolId() != null
                && userDetails.getPersona().getRolId() == 4;
        model.addAttribute("esAcudiente", esAcudiente);
        model.addAttribute("recibo", r);
        model.addAttribute("qrPngBase64", qrCodeService.generarPngBase64(r.getCodigoQrUnico()));
        return "recibo-ver";
    }
}
