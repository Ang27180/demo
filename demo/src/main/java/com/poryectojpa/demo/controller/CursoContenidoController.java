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
            // AJUSTE: Reparación agresiva de la tabla modulo
            // 1. Asegurar que existe la columna id_modulo (por si acaso) y que es la PK
            try {
                jdbcTemplate.execute("ALTER TABLE modulo ADD PRIMARY KEY (id_modulo)");
            } catch (Exception e) {
                // Si ya tiene PK, ignoramos el error
            }
            
            // 2. Forzar el AutoIncrement. Esto es vital para que Hibernate no intente adivinar el ID
            jdbcTemplate.execute("ALTER TABLE modulo MODIFY COLUMN id_modulo INT NOT NULL AUTO_INCREMENT");
            
            // 3. Lo mismo para lecciones
            try {
                jdbcTemplate.execute("ALTER TABLE leccion ADD PRIMARY KEY (id_leccion)");
            } catch (Exception e) {}
            jdbcTemplate.execute("ALTER TABLE leccion MODIFY COLUMN id_leccion INT NOT NULL AUTO_INCREMENT");
            
            System.out.println("--- BASE DE DATOS REPARADA EXITOSAMENTE ---");
        } catch (Exception e) {
            System.err.println("--- AVISO DE BD: " + e.getMessage());
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

    // AJUSTE: Agregar un nuevo módulo (Corregido para evitar errores de duplicidad/transacción)
    @PostMapping("/{id}/modulos")
    public String agregarModulo(@PathVariable("id") Integer id, @ModelAttribute Modulo modulo, 
                               org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        try {
            Curso curso = cursoRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + id));
            
            // --- AJUSTE VITAL: Forzamos el ID a null para que Hibernate lo trate como un registro NUEVO
            // Esto evita el error "Row was updated or deleted by another transaction"
            modulo.setId(null); 
            modulo.setCurso(curso);
            
            moduloRepo.save(modulo);
            return "redirect:/admin/cursos/" + id + "/contenido";
        } catch (Exception e) {
            System.err.println("Error al guardar módulo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error técnico: " + e.getMessage());
            return "redirect:/admin/cursos/" + id + "/contenido";
        }
    }

    // AJUSTE: Agregar una nueva lección (Simplificado con @RequestParam para máxima compatibilidad)
    @PostMapping("/modulos/{moduloId}/lecciones")
    public String agregarLeccion(@PathVariable("moduloId") Integer moduloId, 
                                 @RequestParam("nombre") String nombre,
                                 @RequestParam("contenidoTipo") String contenidoTipo,
                                 @RequestParam(value = "contenidoUrl", required = false) String contenidoUrl,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Integer cursoId = null;
        try {
            Modulo modulo = moduloRepo.findById(moduloId)
                    .orElseThrow(() -> new IllegalArgumentException("Módulo no encontrado: " + moduloId));
            
            cursoId = modulo.getCurso().getId();
            
            System.out.println("--- INTENTANDO GUARDAR LECCIÓN: " + nombre + " EN MÓDULO ID: " + moduloId + " ---");
            
            // AJUSTE: Validar nombre
            if (nombre == null || nombre.trim().isEmpty()) {
                throw new RuntimeException("El nombre de la lección es obligatorio");
            }
            
            // AJUSTE: Crear lección manualmente
            Leccion nuevaLeccion = new Leccion();
            nuevaLeccion.setNombre(nombre);
            nuevaLeccion.setContenidoTipo(contenidoTipo);
            nuevaLeccion.setContenidoUrl(contenidoUrl);
            nuevaLeccion.setModulo(modulo);
            nuevaLeccion.setId(null); // Asegurar que sea INSERT
            
            leccionRepo.save(nuevaLeccion);
            
            return "redirect:/admin/cursos/" + cursoId + "/contenido";
        } catch (Exception e) {
            System.err.println("ERROR AL GUARDAR LECCIÓN: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
            return (cursoId != null) ? "redirect:/admin/cursos/" + cursoId + "/contenido" : "redirect:/admin";
        }
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
