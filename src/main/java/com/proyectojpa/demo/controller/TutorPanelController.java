package com.proyectojpa.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyectojpa.demo.Service.ProgresoLeccionService;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.PersonaRepository;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

/**
 * Panel pedagógico del tutor: cursos asignados y alumnos inscritos.
 */
@Controller
@RequestMapping("/tutor/panel")
public class TutorPanelController {

    private final TutorRepository tutorRepository;
    private final PersonaRepository personaRepository;
    private final cursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final ProgresoLeccionService progresoLeccionService;

    public TutorPanelController(TutorRepository tutorRepository, PersonaRepository personaRepository,
            cursoRepository cursoRepository, InscripcionRepository inscripcionRepository,
            ProgresoLeccionService progresoLeccionService) {
        this.tutorRepository = tutorRepository;
        this.personaRepository = personaRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.progresoLeccionService = progresoLeccionService;
    }

    /**
     * Si la persona tiene rol tutor pero aún no existe fila en {@code tutor}, se crea una mínima
     * (evita error al entrar al panel cuando el usuario se creó solo con rol, sin pasar por CRUD admin).
     */
    private Tutor obtenerOCrearTutor(CustomUserDetails user) {
        Integer pid = user.getPersona().getId();
        Persona persona = personaRepository.findById(pid)
                .orElseThrow(() -> new IllegalStateException("Persona no encontrada."));
        return tutorRepository.findByPersona(persona).orElseGet(() -> {
            Tutor t = new Tutor();
            t.setPersona(persona);
            return tutorRepository.save(t);
        });
    }

    @GetMapping("/cursos")
    public String misCursos(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Tutor tutor = obtenerOCrearTutor(user);
        model.addAttribute("cursos", cursoRepository.findByTutor_IdTutorWithTutorPersona(tutor.getIdTutor()));
        return "tutor-mis-cursos";
    }

    @GetMapping("/cursos/{idCurso}/alumnos")
    public String alumnosDelCurso(@PathVariable Integer idCurso, @AuthenticationPrincipal CustomUserDetails user,
            Model model) {
        Tutor tutor = obtenerOCrearTutor(user);
        var curso = cursoRepository.findByIdAndTutor_IdTutor(idCurso, tutor.getIdTutor())
                .orElseThrow(() -> new IllegalArgumentException("Curso no asignado a este tutor."));

        List<Inscripcion> inscripciones = inscripcionRepository.findByCursoIdWithEstudianteAndEstado(idCurso);
        Map<Integer, Integer> progresoPorInscripcion = new HashMap<>();
        for (Inscripcion i : inscripciones) {
            int pct = progresoLeccionService.calcularPorcentaje(i.getEstudiante(), i.getCurso());
            progresoPorInscripcion.put(i.getId(), pct);
        }

        model.addAttribute("curso", curso);
        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("progresoPorInscripcion", progresoPorInscripcion);
        return "tutor-curso-alumnos";
    }
}
