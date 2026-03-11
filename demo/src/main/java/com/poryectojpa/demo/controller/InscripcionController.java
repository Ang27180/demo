package com.poryectojpa.demo.controller;

import java.time.LocalDate;
import java.util.List; // AJUSTE: Import para listas
import java.util.Collections; // AJUSTE: Import para colecciones

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

import com.poryectojpa.demo.models.Curso;
import com.poryectojpa.demo.models.EstadoInscripcion;
import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.models.Inscripcion;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.EstadoInscripcionRepository;
import com.poryectojpa.demo.repository.EstudianteRepository;
import com.poryectojpa.demo.repository.InscripcionRepository;
import com.poryectojpa.demo.repository.cursoRepository;
import com.poryectojpa.demo.security.CustomUserDetails;

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
    public String nuevaInscripcion(@PathVariable(required = false) Integer idEstudiante, 
                                   @RequestParam(name = "idCurso", required = false) Integer idCurso, // AJUSTE: Corregido nombre a idCurso y añadido @RequestParam
                                   Model model) {

        model.addAttribute("idEstudiante", idEstudiante);
        model.addAttribute("idCurso", idCurso);

        // --- AJUSTE: Si viene un idCurso, mostramos solo ese curso en la lista para evitar confusiones ---
        if (idCurso != null) {
            cursoRepository.findById(idCurso).ifPresent(curso -> {
                model.addAttribute("listaCursos", Collections.singletonList(curso));
            });
        } else {
            // Si no viene idCurso, mostramos todos (funcionalidad original corregida)
            model.addAttribute("listaCursos", cursoRepository.findAll());
        }
        // ------------------------------------------------------------------------------------------------

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
