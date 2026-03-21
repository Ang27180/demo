package com.proyectojpa.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
public class MisCursosController {

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private com.proyectojpa.demo.repository.ReciboRepository reciboRepository;

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

        List<Inscripcion> inscripciones = inscripcionRepo
                .findByEstudianteIdWithCursoAndEstado(estudiante.getIdEstudiante());

        Map<Integer, Boolean> yaTieneRecibo = new HashMap<>();
        Map<Integer, Integer> idReciboPorInscripcion = new HashMap<>();
        for (Inscripcion i : inscripciones) {
            boolean existe = reciboRepository.existsByInscripcion(i);
            yaTieneRecibo.put(i.getId(), existe);
            if (existe) {
                reciboRepository.findByInscripcion(i).ifPresent(r -> idReciboPorInscripcion.put(i.getId(), r.getId()));
            }
        }

        model.addAttribute("inscripciones", inscripciones);
        model.addAttribute("yaTieneRecibo", yaTieneRecibo);
        model.addAttribute("idReciboPorInscripcion", idReciboPorInscripcion);

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
