package com.proyectojpa.demo.controller;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.Service.OrdenPagoService;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.OrdenPago;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

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

    @Autowired
    private EstadoInscripcionRepository estadoInscripcionRepository;

    @Autowired
    private OrdenPagoService ordenPagoService;

    @GetMapping("/mis-cursos")
    public String misCursos(Model model) {

        Persona persona = getPersonaActual();

        if (persona == null) {
            return "redirect:/login";
        }

        // Si es TUTOR (rol 3)
        if (persona.getRolId() != null && persona.getRolId().equals(3)) {
            Tutor tutor = tutorRepository.findByPersona(persona).orElse(null);
            List<Curso> misCursosTutor = (tutor != null)
                    ? cursoRepo.findByTutor_IdTutorWithTutorPersona(tutor.getIdTutor())
                    : new ArrayList<>();
            
            // Convertimos la lista de cursos a una "falsa" lista de inscripciones para reusar la vista misCursos.html
            // o simplemente mandamos la lista de cursos si preferimos ajustar el HTML.
            // Para mantener compatibilidad mínima con el HTML actual (que usa inscripcion.curso)
            List<Inscripcion> falsasInscripciones = new ArrayList<>();
            for (Curso c : misCursosTutor) {
                Inscripcion ins = new Inscripcion();
                ins.setCurso(c);
                // Estado ficticio para que no falle el HTML (inscripcion.estado.nombre)
                com.proyectojpa.demo.models.EstadoInscripcion est = new com.proyectojpa.demo.models.EstadoInscripcion();
                est.setNombre("Tutor/Dueño");
                ins.setEstado(est);
                falsasInscripciones.add(ins);
            }
            model.addAttribute("inscripciones", falsasInscripciones);
            model.addAttribute("esTutor", true);
        } else if (persona.getRolId() != null && persona.getRolId().equals(2)) {
            // Solo rol estudiante: fila en tabla estudiante; acudientes u otros roles no.
            Estudiante estudiante = estudianteRepository
                    .findByPersona(persona)
                    .orElseGet(() -> {
                        Estudiante nuevoEstudiante = new Estudiante();
                        nuevoEstudiante.setPersona(persona);
                        nuevoEstudiante.setProgreso("0%");
                        estadoInscripcionRepository.findByCodigo(InscripcionEstados.ACTIVO)
                                .ifPresent(nuevoEstudiante::setEstadoEstudiante);
                        return estudianteRepository.save(nuevoEstudiante);
                    });

            List<Inscripcion> inscripciones = inscripcionRepo.findByEstudianteIdWithCursoAndEstado(estudiante.getIdEstudiante());
            Map<Integer, OrdenPago> ordenActivaPorInscripcion = ordenPagoService
                    .mapaOrdenActivaPorInscripciones(estudiante.getIdEstudiante());
            model.addAttribute("inscripciones", inscripciones);
            model.addAttribute("ordenActivaPorInscripcion", ordenActivaPorInscripcion);
            model.addAttribute("esTutor", false);
        } else {
            model.addAttribute("inscripciones", new ArrayList<Inscripcion>());
            model.addAttribute("ordenActivaPorInscripcion", Collections.emptyMap());
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
