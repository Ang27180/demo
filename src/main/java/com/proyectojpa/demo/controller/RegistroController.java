package com.proyectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PersonaRepository;

import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class RegistroController {

    @Autowired
    private PersonaRepository PersonaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.proyectojpa.demo.repository.EstudianteRepository estudianteRepository; // AJUSTE: Agregado para crear perfil de estudiante

    @Autowired
    private com.proyectojpa.demo.repository.EstadoInscripcionRepository estadoInscripcionRepository;

    @Autowired
    private Validator validator;

    @GetMapping("/registro")
    public String mostrarFormulario(Model model) {
        Persona persona = new Persona();
        persona.setRolId(2); // estudiante (formulario público: siempre rol 2)
        model.addAttribute("persona", persona);
        return "registro"; // vista registro.html
    }

    @PostMapping("/registro")
    public String procesarFormulario(
            @ModelAttribute("persona") Persona persona,
            BindingResult result,
            Model model) {

        // Rol fijo antes de validar (@NotNull en rolId)
        persona.setRolId(2);
        if (persona.getEmail() != null) {
            persona.setEmail(persona.getEmail().trim().toLowerCase(Locale.ROOT));
        }
        validator.validate(persona, result);
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

        // Guardar persona en la base de datos
        Persona guardada = PersonaRepository.save(persona);

        // AJUSTE: Si el rol es Usuario (2), creamos automáticamente su perfil de estudiante
        if (guardada.getRolId() != null && guardada.getRolId() == 2) {

            com.proyectojpa.demo.models.Estudiante estudiante = new com.proyectojpa.demo.models.Estudiante();
            estudiante.setPersona(guardada);
            estudiante.setProgreso("0%");
            estadoInscripcionRepository.findByCodigo(InscripcionEstados.ACTIVO)
                    .ifPresent(estudiante::setEstadoEstudiante);
            estudianteRepository.save(estudiante);
        }



        // Redirigir al login
        return "redirect:/login";
    }
}
