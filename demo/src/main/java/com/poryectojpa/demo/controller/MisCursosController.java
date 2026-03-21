package com.poryectojpa.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.models.Inscripcion;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.EstudianteRepository;
import com.poryectojpa.demo.repository.InscripcionRepository;
import com.poryectojpa.demo.security.CustomUserDetails;

@Controller
public class MisCursosController {

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @GetMapping("/mis-cursos")
    public String misCursos(Model model) {

        Persona persona = getPersonaActual();

        if (persona == null) {
            return "redirect:/login";
        }

        Estudiante estudiante = estudianteRepository
                .findByPersona(persona)
                .orElseGet(() -> {
                    // AJUSTE: Si no existe el registro de estudiante, lo creamos automáticamente
                    // para evitar el error "La persona no es estudiante"
                    Estudiante nuevoEstudiante = new Estudiante();
                    nuevoEstudiante.setPersona(persona);
                    nuevoEstudiante.setProgreso("0%");
                    nuevoEstudiante.setEstadoEstudiante(1);
                    return estudianteRepository.save(nuevoEstudiante);
                });

        List<Inscripcion> inscripciones = inscripcionRepo.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());

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
