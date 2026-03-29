package com.proyectojpa.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Optional;

/**
 * Controlador de la Vista de Acudiente.
 * Resuelve las variables requeridas por el template acudiente.html
 * e incluye el endpoint para la autorización del certificado final.
 */
@Controller
@RequestMapping("/acudiente/panel")
public class AcudienteController {

    private final AcudienteRepository acudienteRepository;
    private final InscripcionRepository inscripcionRepository;

    public AcudienteController(AcudienteRepository acudienteRepository, InscripcionRepository inscripcionRepository) {
        this.acudienteRepository = acudienteRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    /**
     * Renderiza el panel principal de Acudiente.
     */
    /**
     * Renderiza el panel principal de Acudiente.
     */
    @GetMapping
    public String vistaAcudiente(Model model) {
        // Obtenemos la persona logueada vía SecurityContext (más seguro y consistente)
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (!(principal instanceof com.proyectojpa.demo.security.CustomUserDetails)) {
            return "redirect:/login"; 
        }
        
        Persona persona = ((com.proyectojpa.demo.security.CustomUserDetails) principal).getPersona();
        model.addAttribute("personaSesion", persona);

        // Listar el vínculo formativo (buscar estudiante dependiente)
        List<Acudiente> misAcudientes = acudienteRepository.findByPersonaId(persona.getId());

        if (misAcudientes.isEmpty()) {
            // Usuario sin acudientes asociados
            model.addAttribute("estudiante", null);
            return "acudiente"; 
        }

        // Asignamos arbitrariamente el primer estudiante a la vista para simplificar (ajustable en un futuro)
        Acudiente miAcudiente = misAcudientes.get(0);
        Estudiante estudiante = miAcudiente.getEstudianteDependiente();

        model.addAttribute("estudiante", estudiante);

        if (estudiante != null) {
            // Lista complementaria de acudientes del mismo estudiante 
            List<Acudiente> acudientesEstudiante = acudienteRepository.findByEstudianteDependienteIdEstudiante(estudiante.getIdEstudiante());
            model.addAttribute("acudientes", acudientesEstudiante);

            // Obtener progreso de lecciones asociadas (las inscripciones del estudiante)
            List<Inscripcion> inscripciones = inscripcionRepository.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
            model.addAttribute("inscripciones", inscripciones);
        }

        return "acudiente"; 
    }

    /**
     * Botón para emitir Firma Virtual / Autorización
     * Cambia el parametro 'certificadoAutorizado' a true en la tabla 'inscripcion'
     */
    @PostMapping("/autorizar-certificado/{id}")
    public String autorizarCertificado(@PathVariable Integer id) {
        Optional<Inscripcion> optInsc = inscripcionRepository.findById(id);
        if (optInsc.isPresent()) {
            Inscripcion inscripcion = optInsc.get();
            inscripcion.setCertificadoAutorizado(true); // Autorizado!
            inscripcionRepository.save(inscripcion);
        }
        return "redirect:/acudiente"; 
    }
}
