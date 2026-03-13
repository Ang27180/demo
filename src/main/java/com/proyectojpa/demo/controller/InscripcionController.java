package com.proyectojpa.demo.controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.EstadoInscripcion;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.security.CustomUserDetails;

@Controller
@RequestMapping("/inscripciones")
public class InscripcionController {

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private EstadoInscripcionRepository estadoRepo;

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private EstudianteRepository estudianteRepository;

    // ----------- MOSTRAR FORMULARIO -----------------
    @GetMapping({ "/nueva", "/nueva/{idEstudiante}" })
    public String nuevaInscripcion(@PathVariable(required = false) Integer idEstudiante, Integer id_Curso,
            Model model) {

        model.addAttribute("idEstudiante", idEstudiante);
        model.addAttribute("idCurso", id_Curso);
        model.addAttribute("listaCursos", cursoRepository.findAll());
        model.addAttribute("listaEstados", estadoRepo.findAll());

        return "inscripcion"; // inscripcion.html en templates
    }

    // ------------ GUARDAR INSCRIPCIÓN ----------------
    @PostMapping("/guardar")
    public String guardarInscripcion(@RequestParam Integer idCurso, RedirectAttributes redirectAttributes) {

        // 1. Persona logueada
        Persona personaActual = getPersona();

        if (personaActual == null) {
            return "redirect:/login";
        }

        // 2. Estudiante asociado a la persona
        Estudiante estudiante = estudianteRepository
                .findByPersona(personaActual)
                .orElseGet(() -> {
                    // AJUSTE: Si no existe el registro de estudiante, lo creamos automáticamente
                    // para evitar el error "La persona no está registrada como estudiante"
                    Estudiante nuevoEstudiante = new Estudiante();
                    nuevoEstudiante.setPersona(personaActual);
                    nuevoEstudiante.setProgreso("0%");
                    nuevoEstudiante.setEstadoEstudiante(1); // 1 = Activo por defecto
                    return estudianteRepository.save(nuevoEstudiante);
                });

        // 3. Curso
        @SuppressWarnings("null")
        Curso curso = cursoRepository.findById(idCurso)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));

        // 4. Estado INSCRITO (mejor obtenerlo de BD)
        EstadoInscripcion estado = estadoRepo.findById(1)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado"));

        // VALIDACIÓN: evitar inscripción duplicada
        if (inscripcionRepo.existsByEstudianteAndCurso(estudiante, curso)) {
            return "redirect:/cursos?yaInscrito";
        }

        // 5. Crear inscripción
        Inscripcion insc = new Inscripcion();
        insc.setEstudiante(estudiante);
        insc.setCurso(curso);
        insc.setEstado(estado);
        insc.setFechaInscripcion(LocalDate.now());

        // 6. Guardar
        inscripcionRepo.save(insc);
        redirectAttributes.addFlashAttribute(
        "exito",
        "¡Te has inscrito correctamente en el curso!"
);

        return "redirect:/cursos";
    }

    public Persona getPersonaActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getPersona();
        }

        return null;
    }

    private Persona getPersona() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getPersona();
        }
        return null;
    }

}
