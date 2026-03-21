package com.proyectojpa.demo.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyectojpa.demo.Service.EstudianteService;
import com.proyectojpa.demo.dto.EstudianteDTO;
import com.proyectojpa.demo.models.Estudiante;




@Controller
@RequestMapping("/estudiante")
public class EstudianteController {

    private final EstudianteService estudianteService;
    private final com.proyectojpa.demo.repository.PersonaRepository personaRepository;

    public EstudianteController(EstudianteService estudianteService, 
                                com.proyectojpa.demo.repository.PersonaRepository personaRepository) {
        this.estudianteService = estudianteService;
        this.personaRepository = personaRepository;
    }

    // ============================
    //  VISTA PRINCIPAL
    // ============================
    @GetMapping
    public String vistaEstudiante(@org.springframework.security.core.annotation.AuthenticationPrincipal com.proyectojpa.demo.security.CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            String email = userDetails.getPersona().getEmail();
            com.proyectojpa.demo.models.Persona persona = personaRepository.findByEmail(email);
            
            com.proyectojpa.demo.models.Estudiante estudiante = estudianteService.findByPersona(persona).orElse(null);

            model.addAttribute("estudiante", estudiante);
        }

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
