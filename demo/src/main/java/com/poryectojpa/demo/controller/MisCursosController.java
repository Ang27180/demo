package com.poryectojpa.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.poryectojpa.demo.models.Inscripcion;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.InscripcionRepository;
import com.poryectojpa.demo.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class MisCursosController {

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @GetMapping("/mis-cursos")
    public String misCursos(Model model) {

        Persona persona = getPersonaActual();

        if (persona == null) {
            return "redirect:/login";
        }

        Integer idEstudiante = persona.getId(); // ← ID del estudiante

        List<Inscripcion> inscripciones = inscripcionRepo.findByIdEstudiante(idEstudiante);

        model.addAttribute("inscripciones", inscripciones);

        return "misCursos";
    }

    private Persona getPersonaActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails user) {
            return user.getPersona();
        }
        return null;
    }
}
