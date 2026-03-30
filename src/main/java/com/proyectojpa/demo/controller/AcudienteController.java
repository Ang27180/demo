package com.proyectojpa.demo.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyectojpa.demo.Service.ReciboAutorizacionService;
import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.ReciboRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

/**
 * Panel del acudiente: estudiante dependiente, inscripciones, recibos y autorización de certificados.
 */
@Controller
@RequestMapping("/acudiente/panel")
public class AcudienteController {

    private final AcudienteRepository acudienteRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ReciboRepository reciboRepository;
    private final ReciboAutorizacionService reciboAutorizacionService;

    public AcudienteController(AcudienteRepository acudienteRepository,
            InscripcionRepository inscripcionRepository,
            ReciboRepository reciboRepository,
            ReciboAutorizacionService reciboAutorizacionService) {
        this.acudienteRepository = acudienteRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.reciboRepository = reciboRepository;
        this.reciboAutorizacionService = reciboAutorizacionService;
    }

    @GetMapping
    public String vistaAcudiente(Model model,
            @RequestParam(name = "msg", required = false) String msg,
            @RequestParam(name = "error", required = false) String error) {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        if (!(principal instanceof CustomUserDetails userDetails)) {
            return "redirect:/login";
        }

        Persona persona = userDetails.getPersona();
        model.addAttribute("personaSesion", persona);
        if (msg != null) {
            model.addAttribute("panelMsg", msg);
        }
        if (error != null) {
            model.addAttribute("panelError", error);
        }

        List<Acudiente> misAcudientes = acudienteRepository.findByPersonaIdWithDetalle(persona.getId());

        if (misAcudientes.isEmpty()) {
            model.addAttribute("estudiante", null);
            return "acudiente";
        }

        Estudiante estudiante = misAcudientes.stream()
                .map(Acudiente::getEstudianteDependiente)
                .filter(e -> e != null && e.getIdEstudiante() != null)
                .findFirst()
                .orElse(null);

        model.addAttribute("estudiante", estudiante);

        if (estudiante != null) {
            List<Acudiente> acudientesEstudiante = acudienteRepository
                    .findByEstudianteDependienteIdEstudianteWithDetalle(estudiante.getIdEstudiante());
            model.addAttribute("acudientes", acudientesEstudiante);

            List<Inscripcion> inscripciones = inscripcionRepository
                    .findByEstudianteIdWithCursoAndEstado(estudiante.getIdEstudiante());
            model.addAttribute("inscripciones", inscripciones);

            Map<Integer, List<Recibo>> recibosPorInscripcion = new HashMap<>();
            for (Inscripcion i : inscripciones) {
                recibosPorInscripcion.put(i.getId(),
                        reciboRepository.findAllByInscripcionIdWithDetalle(i.getId()));
            }
            model.addAttribute("recibosPorInscripcion", recibosPorInscripcion);
        }

        return "acudiente";
    }

    /**
     * Autoriza la descarga del certificado (firma digital / consentimiento).
     */
    @PostMapping("/autorizar-certificado/{id}")
    public String autorizarCertificado(@PathVariable Integer id) {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return "redirect:/login";
        }
        Persona persona = userDetails.getPersona();
        Optional<Inscripcion> optInsc = inscripcionRepository.findByIdWithEstudiantePersonaAndCurso(id);
        if (optInsc.isEmpty()) {
            return "redirect:/acudiente/panel?error=certificado";
        }
        Inscripcion inscripcion = optInsc.get();
        if (!reciboAutorizacionService.esAcudienteDeEstudiante(persona, inscripcion.getEstudiante())) {
            return "redirect:/acudiente/panel?error=certificado";
        }
        if (inscripcion.getFechaSolicitudCertificadoAcudiente() == null) {
            return "redirect:/acudiente/panel?error=certificado_solicitud";
        }
        inscripcion.setCertificadoAutorizado(true);
        inscripcion.setFechaAutorizacionCertificado(LocalDateTime.now());
        inscripcionRepository.save(inscripcion);
        return "redirect:/acudiente/panel?msg=certificado_ok";
    }
}
