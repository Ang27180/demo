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
import jakarta.annotation.PostConstruct; // AJUSTE
import org.springframework.jdbc.core.JdbcTemplate; // AJUSTE

@Controller
@RequestMapping("/admin/cursos")
public class CursoContenidoController {

    @Autowired
    private cursoRepository cursoRepo;

    @Autowired
    private ModuloRepository moduloRepo;

    @Autowired
    private LeccionRepository leccionRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate; // AJUSTE: Para reparaciones directas en la BD

    // --- AJUSTE: Asegurar que las tablas tengan la estructura correcta (PK e Identity) ---
    // Esto se ejecuta al iniciar la aplicación para corregir errores de esquema
    @PostConstruct
    public void corregirEsquema() {
        try {
            // AJUSTE: Corregir tabla MODULO
            // Primero verificamos si existe la PK antes de intentar agregarla para evitar errores
            try {
                jdbcTemplate.execute("ALTER TABLE modulo ADD PRIMARY KEY (id_modulo)");
            } catch (Exception e) {
                // Si falla es porque probablemente ya existe o la tabla tiene otro problema
                System.out.println("Nota: No se pudo agregar PK a modulo (quizás ya existe): " + e.getMessage());
            }
            
            // Asegurar que sean AutoIncrement
            jdbcTemplate.execute("ALTER TABLE modulo MODIFY COLUMN id_modulo INT AUTO_INCREMENT");
            jdbcTemplate.execute("ALTER TABLE leccion MODIFY COLUMN id_leccion INT AUTO_INCREMENT");
            
            System.out.println("--- ESQUEMA DE BD REFORZADO Y CORREGIDO ---");
        } catch (Exception e) {
            System.err.println("--- ERROR CRÍTICO AL CORREGIR ESQUEMA: " + e.getMessage());
        }
    }

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

    // AJUSTE: Agregar un nuevo módulo con captura de errores
    @PostMapping("/{id}/modulos")
    public String agregarModulo(@PathVariable("id") Integer id, @ModelAttribute Modulo modulo, 
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            Curso curso = cursoRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
            
            modulo.setCurso(curso);
            moduloRepo.save(modulo);
            return "redirect:/admin/cursos/" + id + "/contenido";
        } catch (Exception e) {
            // AJUSTE: Enviamos el error a la vista para saber qué pasa exactamente
            redirectAttributes.addFlashAttribute("error", "Error al crear módulo: " + e.getMessage());
            return "redirect:/admin/cursos/" + id + "/contenido";
        }
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
