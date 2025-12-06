package com.poryectojpa.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.personaRepository;

@Controller
@RequestMapping("/personas")
public class PersonaController {

    @Autowired
    private personaRepository personaRepository;

    // LISTAR
    @GetMapping
    public String mostrarPersonas(Model model) {
        List<Persona> personas = personaRepository.findAll();
        model.addAttribute("personas", personas);
        return "admin"; // Vista unificada
    }

    // CREAR - mostrar formulario
    @GetMapping("/nueva")
    public String mostrarFormularioNuevaPersona(Model model) {
        model.addAttribute("persona", new Persona());
        return "admin"; // usamos el mismo archivo
    }

    // CREAR - guardar nueva persona
    @PostMapping
    public String guardarPersona(@ModelAttribute Persona persona) {
        personaRepository.save(persona);
        return "redirect:/personas";
    }

    // EDITAR - mostrar formulario con datos
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Integer id, Model model) {
        Persona persona = personaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + id));
        model.addAttribute("persona", persona);
        return "admin"; 
    }

    // EDITAR - actualizar
    @PostMapping("/{id}")
    public String actualizarPersona(@PathVariable Integer id, @ModelAttribute Persona persona) {
        persona.setId(id);
        personaRepository.save(persona);
        return "redirect:/personas";
    }

    // ELIMINAR
    @GetMapping("/eliminar/{id}")
    public String eliminarPersona(@PathVariable Integer id) {
        personaRepository.deleteById(id);
        return "redirect:/personas";
    }
}
