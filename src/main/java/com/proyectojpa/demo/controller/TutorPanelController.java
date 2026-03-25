package com.poryectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.poryectojpa.demo.models.Inscripcion;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.models.Tutor;
import com.poryectojpa.demo.repository.InscripcionRepository;
import com.poryectojpa.demo.repository.TutorRepository;
import com.poryectojpa.demo.security.CustomUserDetails;

import java.util.List;
import java.util.ArrayList;

@Controller
public class TutorPanelController {

    @Autowired
    private TutorRepository tutorRepo;

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @GetMapping("/tutor-panel")
    public String dashboard(Model model) {
        Persona persona = getPersonaActual();
        if (persona == null) return "redirect:/login";
        model.addAttribute("persona", persona);
        return "tutor";
    }

    @GetMapping("/tutor-panel-estudiantes")
    public String verEstudiantes(Model model) {
        Persona persona = getPersonaActual();
        if (persona == null) return "redirect:/login";

        Tutor tutor = tutorRepo.findByPersona(persona).orElse(null);
        List<Inscripcion> inscripciones = (tutor != null) 
            ? inscripcionRepo.findByCurso_Tutor_IdTutor(tutor.getIdTutor()) 
            : new ArrayList<>();

        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("persona", persona);
        
        return "tutor-estudiantes";
    }

    private Persona getPersonaActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user.getPersona();
        }
        return null;
    }
}
