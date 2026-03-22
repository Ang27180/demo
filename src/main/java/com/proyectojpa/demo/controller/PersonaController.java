package com.proyectojpa.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Controller
@RequestMapping("/personas")
public class PersonaController {

    @Autowired
    private PersonaRepository PersonaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Autowired
    private EstadoInscripcionRepository estadoInscripcionRepository;

    // LISTAR
    @GetMapping
    public String mostrarPersonas(Model model) {
        List<Persona> personas = PersonaRepository.findAllWithEstudianteEstado();
        model.addAttribute("personas", personas);
        return "lista"; // archivo lista.html
    }

    // CREAR - mostrar formulario
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaPersona(Model model) {
        model.addAttribute("persona", new Persona());
        return "formulario";
    }

    // CREAR - guardar nueva persona
    @PostMapping
    public String guardarPersona(@ModelAttribute Persona persona) {
        PersonaRepository.save(persona);
        return "redirect:/personas";
    }

    // EDITAR - mostrar formulario con datos
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Persona persona = PersonaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
        model.addAttribute("persona", persona);
        return "formulario";
    }

    // EDITAR - actualizar
    @PostMapping("/{id}")
    public String actualizarPersona(@PathVariable Integer id, @ModelAttribute Persona persona) {
        persona.setId(id);
        PersonaRepository.save(persona);
        return "redirect:/personas";
    }

    // ELIMINAR
    @GetMapping("/eliminar/{id}")
    public String eliminarPersona(@PathVariable Integer id) {
        PersonaRepository.deleteById(id);
        return "redirect:/personas";
    }

    /** Activa/desactiva cuenta de estudiante (estado_inscripcion ACTIVO ↔ INACTIVO). */
    @PostMapping("/{id}/toggle-estado-cuenta")
    public String toggleEstadoCuentaEstudiante(@PathVariable Integer id) {
        Persona p = PersonaRepository.findById(id).orElseThrow();
        if (p.getRolId() == null || p.getRolId() != 2) {
            return "redirect:/personas";
        }
        Estudiante e = estudianteRepository.findByPersona(p).orElse(null);
        if (e == null) {
            return "redirect:/personas";
        }
        boolean esActivo = e.getEstadoEstudiante() != null
                && InscripcionEstados.ACTIVO.equals(e.getEstadoEstudiante().getCodigo());
        String destino = esActivo ? InscripcionEstados.INACTIVO : InscripcionEstados.ACTIVO;
        estadoInscripcionRepository.findByCodigo(destino).ifPresent(est -> {
            e.setEstadoEstudiante(est);
            estudianteRepository.save(e);
        });
        return "redirect:/personas";
    }
}
