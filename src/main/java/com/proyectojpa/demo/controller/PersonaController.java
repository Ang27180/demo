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

import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PersonaRepository;

/**
 * PersonaController maneja la gestión (CRUD) de usuarios del sistema desde el panel de administración.
 */
@Controller
@RequestMapping("/personas")
public class PersonaController {

    @Autowired
    private PersonaRepository personaRepository; // Corregido el nombre a PersonaRepository (mayúscula)

    // LISTAR todas las personas registradas
    @GetMapping
    public String mostrarPersonas(Model model) {
        List<Persona> personas = personaRepository.findAll();
        model.addAttribute("personas", personas);
        return "lista"; // archivo lista.html
    }

    // CREAR - mostrar formulario vacío
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaPersona(Model model) {
        model.addAttribute("persona", new Persona());
        return "formulario";
    }

    // CREAR - guardar nueva persona y redirigir al panel admin
    @PostMapping
    public String guardarPersona(@ModelAttribute Persona persona) {
        personaRepository.save(persona);
        return "redirect:/admin";
    }

    // EDITAR - mostrar formulario con datos de la persona por ID
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
        model.addAttribute("persona", persona);
        return "formulario";
    }

    // EDITAR - recibir datos y actualizar la persona en BD
    @PostMapping("/{id}")
    public String actualizarPersona(@PathVariable Integer id, @ModelAttribute Persona persona) {
        persona.setId(id);
        personaRepository.save(persona);
        return "redirect:/admin";
    }

    // ELIMINAR una persona
    @GetMapping("/eliminar/{id}")
    public String eliminarPersona(@PathVariable Integer id) {
        personaRepository.deleteById(id);
        return "redirect:/admin";
    }
}
