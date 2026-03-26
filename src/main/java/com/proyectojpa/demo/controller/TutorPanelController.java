package com.proyectojpa.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.proyectojpa.demo.Service.ProgresoLeccionService;
import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
public class TutorPanelController {

    @Autowired
    private TutorRepository tutorRepo;

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private cursoRepository cursoRepo;

    @Autowired
    private ProgresoLeccionService progresoLeccionService;

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

    /** Listado de cursos del tutor (enlaces del layout y post-login opcional). */
    @GetMapping("/tutor/panel/cursos")
    public String misCursosPanel(Model model) {
        Persona persona = getPersonaActual();
        if (persona == null) {
            return "redirect:/login";
        }
        Tutor tutor = tutorRepo.findByPersona(persona).orElse(null);
        List<Curso> cursos = (tutor != null)
                ? cursoRepo.findByTutor_IdTutorWithTutorPersona(tutor.getIdTutor())
                : new ArrayList<>();
        model.addAttribute("cursos", cursos);
        model.addAttribute("persona", persona);
        return "tutor-mis-cursos";
    }

    /** Alumnos inscritos en un curso (solo si el curso pertenece al tutor autenticado). */
    @GetMapping("/tutor/panel/cursos/{idCurso}/alumnos")
    public String alumnosDelCurso(@PathVariable("idCurso") Integer idCurso, Model model) {
        Persona persona = getPersonaActual();
        if (persona == null) {
            return "redirect:/login";
        }
        Tutor tutor = tutorRepo.findByPersona(persona).orElse(null);
        if (tutor == null) {
            return "redirect:/tutor-panel";
        }
        return cursoRepo.findByIdAndTutor_IdTutor(idCurso, tutor.getIdTutor()).map(curso -> {
            List<Inscripcion> inscripciones = inscripcionRepo.findByCursoIdWithEstudianteAndEstado(idCurso);
            Map<Integer, Integer> progresoPorInscripcion = new HashMap<>();
            for (Inscripcion i : inscripciones) {
                progresoPorInscripcion.put(i.getId(),
                        progresoLeccionService.calcularPorcentaje(i.getEstudiante(), curso));
            }
            model.addAttribute("curso", curso);
            model.addAttribute("inscripciones", inscripciones);
            model.addAttribute("progresoPorInscripcion", progresoPorInscripcion);
            model.addAttribute("persona", persona);
            return "tutor-curso-alumnos";
        }).orElse("redirect:/tutor/panel/cursos");
    }

    private Persona getPersonaActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            return user.getPersona();
        }
        return null;
    }
}
