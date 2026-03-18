package com.proyectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyectojpa.demo.repository.cursoRepository;
import com.proyectojpa.demo.models.Curso;

@Controller
@RequestMapping("/cursos")
public class CursoController {

    @Autowired
    private cursoRepository cursoRepository;

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
        return "ver-curso";
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
