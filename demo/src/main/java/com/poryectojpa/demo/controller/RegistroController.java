package com.poryectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.personaRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;

@Controller
public class RegistroController {

    @Autowired
    private personaRepository personaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.poryectojpa.demo.repository.EstudianteRepository estudianteRepository; // AJUSTE: Agregado para crear perfil de estudiante

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        model.addAttribute("persona", new Persona());
        return "registro"; // vista registro.html
    }

    @PostMapping("/registro")
    public String procesarFormulario(
            @Valid @ModelAttribute("persona") Persona persona,
            BindingResult result,
            Model model) {

        // Validaciones de Spring
        if (result.hasErrors()) {
            return "registro";
        }

        // Validar si el correo ya existe
        if (personaRepository.findByEmail(persona.getEmail()) != null) {
            model.addAttribute("errorCorreo", "El correo ya está registrado");
            return "registro";
        }

        // Validar confirmación de contraseña
        if (!persona.getContrasena().equals(persona.getConfirmarContrasena())) {
            model.addAttribute("errorContrasena", "Las contraseñas no coinciden");
            return "registro";
        }

        // Validar aceptación de términos
        if (persona.getAceptaTerminos() == null || !persona.getAceptaTerminos()) {
            model.addAttribute("errorTerminos", "Debes aceptar los términos y condiciones");
            return "registro";
        }

        // Cifrar la contraseña antes de guardar
        persona.setContrasena(passwordEncoder.encode(persona.getContrasena()));

        // Guardar persona en la base de datos
        Persona guardada = personaRepository.save(persona);

        // AJUSTE: Si el rol es Usuario (2), creamos automáticamente su perfil de estudiante
        if (guardada.getRolId() != null && guardada.getRolId() == 2) {
            com.poryectojpa.demo.models.Estudiante estudiante = new com.poryectojpa.demo.models.Estudiante();
            estudiante.setPersona(guardada);
            estudiante.setProgreso("0%");
            estudiante.setEstadoEstudiante(1);
            estudianteRepository.save(estudiante);
        }

        // Redirigir al login
        return "redirect:/login";
    }
}
