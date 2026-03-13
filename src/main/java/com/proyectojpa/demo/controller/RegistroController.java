package com.proyectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PersonaRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.validation.Valid;

@Controller
public class RegistroController {

    @Autowired
    private PersonaRepository PersonaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.proyectojpa.demo.repository.EstudianteRepository estudianteRepository; // AJUSTE: Agregado para crear perfil de estudiante

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
        if (PersonaRepository.findByEmail(persona.getEmail()) != null) {
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

        System.out.println("DEBUG: Procesando registro para " + persona.getEmail() + ", Rol recibido: " + persona.getRolId());

        // Guardar persona en la base de datos
        Persona guardada = PersonaRepository.save(persona);

        System.out.println("DEBUG: Persona guardada con ID: " + guardada.getId() + ", Rol guardado: " + guardada.getRolId());

        // AJUSTE: Si el rol es Usuario (2), creamos automáticamente su perfil de estudiante
        if (guardada.getRolId() != null && guardada.getRolId() == 2) {

            System.out.println("DEBUG: Creando perfil de estudiante para " + guardada.getEmail());
            System.out.println("DEBUG: Datos tutor recibidos: " + persona.getTutorNombre() + ", " + persona.getTutorTelefono());
            
            com.proyectojpa.demo.models.Estudiante estudiante = new com.proyectojpa.demo.models.Estudiante();
            estudiante.setPersona(guardada);
            estudiante.setProgreso("0%");
            estudiante.setEstadoEstudiante(1);
            estudiante.setTutorNombre(persona.getTutorNombre());
            estudiante.setTutorTelefono(persona.getTutorTelefono());
            estudiante.setTutorEmail(persona.getTutorEmail());
            estudianteRepository.save(estudiante);
            System.out.println("DEBUG: Estudiante guardado con éxito.");
        }



        // Redirigir al login
        return "redirect:/login";
    }
}
