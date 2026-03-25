package com.poryectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.validation.BindingResult;

import com.poryectojpa.demo.repository.cursoRepository;
import com.poryectojpa.demo.repository.InscripcionRepository;
import com.poryectojpa.demo.repository.EstudianteRepository;
import com.poryectojpa.demo.repository.TutorRepository;
import com.poryectojpa.demo.models.Curso;
import com.poryectojpa.demo.models.Inscripcion;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.security.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Optional;

@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private cursoRepository cursoRepository;

    @Autowired
    private InscripcionRepository inscripcionRepo;

    @Autowired
    private EstudianteRepository estudianteRepo;

    @Autowired
    private TutorRepository tutorRepository;

    // Listar cursos pública (ya existente)
    @GetMapping
    public String listarCursos(Model model) {
        model.addAttribute("cursos", cursoRepository.findAll());
        return "cursos";
    }

    // Ver información del curso pública (ya existente)
    @GetMapping("/{id}")
    public String verInformacionCurso(@PathVariable("id") Integer id, Model model) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        
        model.addAttribute("curso", curso);

        Persona persona = getPersonaActual();
        if (persona != null) {
            Optional<Estudiante> estudianteOpt = estudianteRepo.findByPersona(persona);
            if (estudianteOpt.isPresent()) {
                Optional<Inscripcion> inscripcionOpt = inscripcionRepo.findByEstudianteAndCurso(estudianteOpt.get(), curso);
                if (inscripcionOpt.isPresent()) {
                    model.addAttribute("inscripcion", inscripcionOpt.get());
                }
            }
        }

        return "ver-curso";
    }

    // --- MÉTODOS PARA ADMINISTRACIÓN (Uncommented y arreglados) ---

    @GetMapping("/admin/form")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("curso", new Curso());
        model.addAttribute("tutores", tutorRepository.findAll());
        return "admin-cursos-form"; // Mantendremos el nombre plano si no hay directorios
    }

    @PostMapping("/admin/guardar")
    public String guardarCurso(@ModelAttribute("curso") Curso curso, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("tutores", tutorRepository.findAll());
            return "admin-cursos-form";
        }
        cursoRepository.save(curso);
        return "redirect:/admin";
    }

    @GetMapping("/admin/editar/{id}")
    public String editarCurso(@PathVariable("id") Integer id, Model model) {
        Curso curso = cursoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Curso inválido"));
        model.addAttribute("curso", curso);
        model.addAttribute("tutores", tutorRepository.findAll());
        return "admin-cursos-form";
    }

    @GetMapping("/admin/eliminar/{id}")
    public String eliminarCurso(@PathVariable("id") Integer id) {
        cursoRepository.deleteById(id);
        return "redirect:/admin";
    }

    private Persona getPersonaActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails user) {
            return user.getPersona();
        }
        return null;
    }
}
