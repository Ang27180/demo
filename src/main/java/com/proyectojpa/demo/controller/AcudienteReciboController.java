package com.proyectojpa.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.Service.QrCodeService;
import com.proyectojpa.demo.Service.ReciboAutorizacionService;
import com.proyectojpa.demo.Service.ReciboService;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.MedioPagoRepository;
import com.proyectojpa.demo.repository.ReciboRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

/**
 * Generación de recibos de pago por parte del acudiente (estudiantes con vínculo en tabla {@code acudiente}).
 */
@Controller
@RequestMapping("/acudiente/pagos/recibo")
public class AcudienteReciboController {

    private final InscripcionRepository inscripcionRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final ReciboRepository reciboRepository;
    private final ReciboService reciboService;
    private final ReciboAutorizacionService reciboAutorizacionService;
    private final QrCodeService qrCodeService;

    public AcudienteReciboController(InscripcionRepository inscripcionRepository,
            MedioPagoRepository medioPagoRepository,
            ReciboRepository reciboRepository,
            ReciboService reciboService,
            ReciboAutorizacionService reciboAutorizacionService,
            QrCodeService qrCodeService) {
        this.inscripcionRepository = inscripcionRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.reciboRepository = reciboRepository;
        this.reciboService = reciboService;
        this.reciboAutorizacionService = reciboAutorizacionService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/nuevo/{idInscripcion}")
    public String formulario(@PathVariable Integer idInscripcion, Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        Inscripcion insc = inscripcionRepository.findByIdWithEstudiantePersonaAndCurso(idInscripcion).orElseThrow();
        if (!reciboAutorizacionService.esAcudienteDeEstudiante(userDetails.getPersona(), insc.getEstudiante())) {
            return "redirect:/acudiente/panel?error=recibo";
        }
        model.addAttribute("inscripcion", insc);
        model.addAttribute("estudianteNombre", insc.getEstudiante().getPersona().getNombre());
        model.addAttribute("medios", medioPagoRepository.findAllWithAdmin());

        List<Recibo> yaGenerados = reciboRepository.findAllByInscripcionIdWithDetalle(idInscripcion);
        model.addAttribute("recibosExistentes", yaGenerados);
        if (!yaGenerados.isEmpty()) {
            Map<Integer, String> qrs = new HashMap<>();
            for (Recibo r : yaGenerados) {
                qrs.put(r.getId(), qrCodeService.generarPngBase64(r.getCodigoQrUnico()));
            }
            model.addAttribute("qrsRecibos", qrs);
        }

        return "acudiente/recibo-nuevo";
    }

    @PostMapping("/generar")
    public String generar(@RequestParam Integer idInscripcion, @RequestParam Integer idMedioPago,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            List<Recibo> previos = reciboRepository.findAllByInscripcionIdWithDetalle(idInscripcion);
            if (!previos.isEmpty()) {
                redirectAttributes.addFlashAttribute("msgRecibo",
                        "Ya existe un recibo para esta inscripción. Puedes verlo abajo.");
                return "redirect:/acudiente/pagos/recibo/nuevo/" + idInscripcion;
            }
            Recibo r = reciboService.generarReciboComoAcudiente(userDetails.getPersona(), idInscripcion, idMedioPago);
            redirectAttributes.addFlashAttribute("msgRecibo", "Recibo generado. Código: " + r.getCodigoQrUnico());
            return "redirect:/recibos/" + r.getId() + "/ver";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorRecibo", e.getMessage());
            return "redirect:/acudiente/pagos/recibo/nuevo/" + idInscripcion;
        }
    }
}
