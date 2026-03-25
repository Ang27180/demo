package com.poryectojpa.demo.controller;

import com.poryectojpa.demo.models.Curso;
import com.poryectojpa.demo.models.Leccion;
import com.poryectojpa.demo.models.Modulo;
import com.poryectojpa.demo.repository.LeccionRepository;
import com.poryectojpa.demo.repository.ModuloRepository;
import com.poryectojpa.demo.repository.cursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/cursos")
public class CursoContenidoController {

    @Autowired
    private cursoRepository cursoRepo;

    @Autowired
    private ModuloRepository moduloRepo;

    @Autowired
    private LeccionRepository leccionRepo;

    // --- AJUSTE: Gestionar contenido de un curso (Módulos y Lecciones) ---
    @GetMapping("/{id}/contenido")
    public String gestionarContenido(@PathVariable("id") Integer id, Model model) {
        Curso curso = cursoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        
        model.addAttribute("curso", curso);
        model.addAttribute("nuevoModulo", new Modulo());
        model.addAttribute("nuevaLeccion", new Leccion());
        
        return "gestion-contenido";
    }

    // AJUSTE: Agregar un nuevo módulo
    @PostMapping("/{id}/modulos")
    public String agregarModulo(@PathVariable("id") Integer id, @ModelAttribute Modulo modulo) {
        Curso curso = cursoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
        
        modulo.setCurso(curso);
        moduloRepo.save(modulo);
        
        return "redirect:/admin/cursos/" + id + "/contenido";
    }

    // AJUSTE: Agregar una nueva lección a un módulo
    @PostMapping("/modulos/{moduloId}/lecciones")
    public String agregarLeccion(@PathVariable("moduloId") Integer moduloId, @ModelAttribute Leccion leccion) {
        Modulo modulo = moduloRepo.findById(moduloId)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + moduloId));
        
        leccion.setModulo(modulo);
        leccionRepo.save(leccion);
        
        return "redirect:/admin/cursos/" + modulo.getCurso().getId() + "/contenido";
    }

    // AJUSTE: Eliminar un módulo
    @GetMapping("/modulos/eliminar/{id}")
    public String eliminarModulo(@PathVariable("id") Integer id) {
        Modulo modulo = moduloRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + id));
        Integer cursoId = modulo.getCurso().getId();
        moduloRepo.delete(modulo);
        return "redirect:/admin/cursos/" + cursoId + "/contenido";
    }

    // AJUSTE: Eliminar una lección
    @GetMapping("/lecciones/eliminar/{id}")
    public String eliminarLeccion(@PathVariable("id") Integer id) {
        Leccion leccion = leccionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lección no encontrada: " + id));
        Integer cursoId = leccion.getModulo().getCurso().getId();
        leccionRepo.delete(leccion);
        return "redirect:/admin/cursos/" + cursoId + "/contenido";
    }
}
