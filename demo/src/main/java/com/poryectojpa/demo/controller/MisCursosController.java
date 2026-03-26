package com.poryectojpa.demo.controller;

import java.util.List;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.models.Inscripcion;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.models.Tutor;
import com.poryectojpa.demo.models.Curso;
import com.poryectojpa.demo.repository.EstudianteRepository;
import com.poryectojpa.demo.repository.InscripcionRepository;
import com.poryectojpa.demo.repository.TutorRepository;
import com.poryectojpa.demo.repository.cursoRepository;
import com.poryectojpa.demo.security.CustomUserDetails;

@Controller
public class MisCursosController {

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private TutorRepository tutorRepository;

    @Autowired
    private cursoRepository cursoRepo;

    @GetMapping("/mis-cursos")
    public String misCursos(Model model) {

        Persona persona = getPersonaActual();

        if (persona == null) {
            return "redirect:/login";
        }

        // Si es TUTOR (rol 3)
        if (persona.getRolId() != null && persona.getRolId().equals(3)) {
            Tutor tutor = tutorRepository.findByPersona(persona).orElse(null);
            List<Curso> misCursosTutor = (tutor != null) ? cursoRepo.findByTutor(tutor) : new ArrayList<>();
            
            // Convertimos la lista de cursos a una "falsa" lista de inscripciones para reusar la vista misCursos.html
            // o simplemente mandamos la lista de cursos si preferimos ajustar el HTML.
            // Para mantener compatibilidad mínima con el HTML actual (que usa inscripcion.curso)
            List<Inscripcion> falsasInscripciones = new ArrayList<>();
            for (Curso c : misCursosTutor) {
                Inscripcion ins = new Inscripcion();
                ins.setCurso(c);
                // Estado ficticio para que no falle el HTML (inscripcion.estado.nombre)
                com.poryectojpa.demo.models.EstadoInscripcion est = new com.poryectojpa.demo.models.EstadoInscripcion();
                est.setNombre("Tutor/Dueño");
                ins.setEstado(est);
                falsasInscripciones.add(ins);
            }
            model.addAttribute("inscripciones", falsasInscripciones);
            model.addAttribute("esTutor", true);
        } else {
            // Lógica original para Estudiante
            Estudiante estudiante = estudianteRepository
                    .findByPersona(persona)
                    .orElseGet(() -> {
                        Estudiante nuevoEstudiante = new Estudiante();
                        nuevoEstudiante.setPersona(persona);
                        nuevoEstudiante.setProgreso("0%");
                        nuevoEstudiante.setEstadoEstudiante(1);
                        return estudianteRepository.save(nuevoEstudiante);
                    });

            List<Inscripcion> inscripciones = inscripcionRepo.findByEstudiante_IdEstudiante(estudiante.getIdEstudiante());
            model.addAttribute("inscripciones", inscripciones);
            model.addAttribute("esTutor", false);
        }

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
