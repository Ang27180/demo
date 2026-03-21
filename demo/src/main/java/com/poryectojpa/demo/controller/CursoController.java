package com.poryectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.poryectojpa.demo.repository.cursoRepository;
import com.poryectojpa.demo.repository.InscripcionRepository; // AJUSTE: Repositorio para buscar inscripciones
import com.poryectojpa.demo.repository.EstudianteRepository; // AJUSTE: Repositorio para buscar estudiantes
import com.poryectojpa.demo.models.Curso;
import com.poryectojpa.demo.models.Inscripcion; // AJUSTE: Modelo de Inscripción
import com.poryectojpa.demo.models.Persona; // AJUSTE: Modelo de Persona
import com.poryectojpa.demo.models.Estudiante; // AJUSTE: Modelo de Estudiante
import com.poryectojpa.demo.security.CustomUserDetails; // AJUSTE: Detalle de usuario para obtener persona actual
import org.springframework.security.core.context.SecurityContextHolder; // AJUSTE: Contexto de seguridad
import java.util.Optional; // AJUSTE: Clase Optional

@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepo; // AJUSTE: Inyectar repositorio de inscripciones

    @Autowired
    private EstudianteRepository estudianteRepo; // AJUSTE: Inyectar repositorio de estudiantes

    // Listar cursos
    @GetMapping
    public String listarCursos(Model model) {
        model.addAttribute("cursos", cursoRepository.findAll());
        return "cursos";
    }

    // Ver información del curso
    @GetMapping("/{id}")
    public String verInformacionCurso(@org.springframework.web.bind.annotation.PathVariable("id") Integer id, Model model) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        
        model.addAttribute("curso", curso);

        // --- AJUSTE: Buscar si el usuario actual está inscrito en este curso para habilitar certificados ---
        Persona persona = getPersonaActual();
        if (persona != null) {
            Optional<Estudiante> estudianteOpt = estudianteRepo.findByPersona(persona);
            if (estudianteOpt.isPresent()) {
                // Buscamos la inscripción del estudiante para este curso específico
                Optional<Inscripcion> inscripcionOpt = inscripcionRepo.findByEstudianteAndCurso(estudianteOpt.get(), curso);
                if (inscripcionOpt.isPresent()) {
                    // Si el estudiante está inscrito, enviamos el objeto inscripcion a la vista
                    model.addAttribute("inscripcion", inscripcionOpt.get());
                }
            }
        }
        // --------------------------------------------------------------------------------------------------

        return "ver-curso";
    }

    // AJUSTE: Método privado para obtener la persona logueada actualmente
    private Persona getPersonaActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return user.getPersona();
        }
        return null;
    }

    // // Mostrar formulario de creación
    // @GetMapping("/nuevo")
    // public String mostrarFormularioNuevo(Model model) {
    //     model.addAttribute("curso", new Curso());
    //     return "admin/cursos/form";
    // }

    // // Guardar curso
    // @PostMapping("/guardar")
    // public String guardarCurso(@Valid @ModelAttribute("curso") Curso curso,
    //                            BindingResult result,
    //                            Model model) {
    //     if (result.hasErrors()) {
    //         return "admin/cursos/form";
    //     }
    //     cursoRepository.save(curso);
    //     return "redirect:/admin/cursos";
    // }

    // // Editar curso
    // @GetMapping("/editar/{id}")
    // public String editarCurso(@PathVariable("id") Integer id, Model model) {
    //     Curso curso = cursoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Curso inválido"));
    //     model.addAttribute("curso", curso);
    //     return "admin/cursos/form";
    // }

    // // Eliminar curso
    // @GetMapping("/eliminar/{id}")
    // public String eliminarCurso(@PathVariable("id") Integer id) {
    //     Curso curso = cursoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Curso inválido"));
    //     cursoRepository.delete(curso);
    //     return "redirect:/admin/cursos";
    // }
}
