package com.poryectojpa.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.poryectojpa.demo.Service.EstudianteService;
import com.poryectojpa.demo.dto.EstudianteDTO;
import com.poryectojpa.demo.models.Estudiante;




@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    // ============================
    //  VISTA PRINCIPAL
    // ============================
    @GetMapping
    public String vistaEstudiante(Model model) {
        model.addAttribute("usuario", "Juan Pérez");

        // Puedes cargar más info del estudiante si lo deseas
        List<Estudiante> lista = estudianteService.findAll();
        model.addAttribute("estudiantes", lista);

        return "Estudiante"; // Renderiza Estudiante.html
    }

    // ============================
    //  VISTA MIS CURSOS
    // ============================
    @GetMapping("/miscursos")
    public String misCursos(Model model) {
        // Aquí cargarías cursos del estudiante real
        model.addAttribute("usuario", "Juan Pérez");
        return "MisCursos";
    }

    // ============================
    //  LISTAR ESTUDIANTES
    // ============================
    @GetMapping("/listar")
    public String listar(Model model) {
        List<Estudiante> lista = estudianteService.findAll();
        model.addAttribute("estudiantes", lista);
        return "EstudianteLista"; // -> estudianteLista.html
    }

    // ============================
    //  FORMULARIO CREAR ESTUDIANTE
    // ============================
    @GetMapping("/crear")
    public String crearFormulario(Model model) {
        model.addAttribute("estudiante", new EstudianteDTO());
        return "EstudianteForm"; // -> estudianteForm.html
    }

    // ============================
    //  GUARDAR ESTUDIANTE
    // ============================
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute EstudianteDTO dto) {
        Estudiante e = new Estudiante();
        //e.setContraseña(dto.getContraseña());
        e.setProgreso(dto.getProgreso());

        // Si tienes Persona y EstadoEstudiante, los asignas aquí usando sus servicios

        estudianteService.save(e);
        return "redirect:/estudiante/listar";
    }

    // ============================
    //  ELIMINAR ESTUDIANTE
    // ============================
    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        estudianteService.delete(id);
        return "redirect:/estudiante/listar";
    }
}
