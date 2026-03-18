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

@Controller
@RequestMapping("/personas")
public class PersonaController {

    @Autowired
    private PersonaRepository PersonaRepository;

    // LISTAR
    @GetMapping
    public String mostrarPersonas(Model model) {
        List<Persona> personas = PersonaRepository.findAll();
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
}
