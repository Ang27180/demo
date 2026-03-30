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
import com.proyectojpa.demo.Service.ReciboNotificacionAcudienteService;
import com.proyectojpa.demo.Service.ReciboService;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.MedioPagoRepository;
import com.proyectojpa.demo.repository.ReciboRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/mis-cursos/recibo")
public class ReciboAlumnoController {

    private final InscripcionRepository inscripcionRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final ReciboRepository reciboRepository;
    private final ReciboService reciboService;
    private final QrCodeService qrCodeService;
    private final ReciboNotificacionAcudienteService reciboNotificacionAcudienteService;

    public ReciboAlumnoController(InscripcionRepository inscripcionRepository,
            MedioPagoRepository medioPagoRepository,
            ReciboRepository reciboRepository,
            ReciboService reciboService,
            QrCodeService qrCodeService,
            ReciboNotificacionAcudienteService reciboNotificacionAcudienteService) {
        this.inscripcionRepository = inscripcionRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.reciboRepository = reciboRepository;
        this.reciboService = reciboService;
        this.qrCodeService = qrCodeService;
        this.reciboNotificacionAcudienteService = reciboNotificacionAcudienteService;
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
        model.addAttribute("requiereAcudienteParaRecibo",
                reciboService.tieneAcudienteVinculado(insc.getEstudiante()));

        List<Recibo> yaGenerados = reciboRepository.findAllByInscripcionIdWithDetalle(idInscripcion);
        model.addAttribute("recibosExistentes", yaGenerados);
        if (!yaGenerados.isEmpty()) {
            Map<Integer, String> qrs = new HashMap<>();
            for (Recibo r : yaGenerados) {
                qrs.put(r.getId(), qrCodeService.generarPngBase64(r.getCodigoQrUnico()));
            }
            model.addAttribute("qrsRecibos", qrs);
        }

        return "recibo-nuevo";
    }

    @PostMapping("/solicitar-acudiente")
    public String solicitarNotificacionAcudiente(@RequestParam Integer idInscripcion,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            int n = reciboNotificacionAcudienteService.enviarSolicitudPagoAAcudientes(userDetails.getPersona(),
                    idInscripcion);
            redirectAttributes.addFlashAttribute("msgRecibo",
                    "Se envió el aviso por correo a tu acudiente (" + n + " destinatario(s)). "
                            + "Debe iniciar sesión para generar el recibo de pago.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorRecibo", e.getMessage());
        }
        return "redirect:/mis-cursos/recibo/nuevo/" + idInscripcion;
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
                Recibo primero = previos.get(0);
                redirectAttributes.addFlashAttribute("msgRecibo",
                        "Ya tenías un recibo para esta inscripción. Puedes verlo y descargarlo abajo.");
                return "redirect:/mis-cursos/recibo/nuevo/" + idInscripcion;
            }
            Recibo r = reciboService.generarRecibo(userDetails.getPersona(), idInscripcion, idMedioPago);
            redirectAttributes.addFlashAttribute("msgRecibo", "Recibo generado. Código: " + r.getCodigoQrUnico());
            return "redirect:/recibos/" + r.getId() + "/ver";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorRecibo", e.getMessage());
            return "redirect:/mis-cursos/recibo/nuevo/" + idInscripcion;
        }
    }
}
