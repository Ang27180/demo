package com.proyectojpa.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyectojpa.demo.dto.LoginDTO;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PersonaRepository;

@Controller
public class LoginController {

    @Autowired
    private PersonaRepository PersonaRepository;

    @GetMapping("/login")
    public String mostrarLogin(Model model, @RequestParam(name = "curso", required = false) Integer curso) {
        model.addAttribute("login", new LoginDTO());
        if (curso != null) {
            model.addAttribute("cursoInscripcionId", curso);
        }
        return "login";
    }

    // POST /login lo procesa Spring Security (DaoAuthenticationProvider + BCrypt).
    // No usar comprobación en claro aquí: chocaría con contraseñas cifradas en BD.
    @GetMapping("/forgot-password")
    public String mostrarRecuperarContrasena() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String procesarRecuperarContrasena(@RequestParam String email, Model model) {
        Persona user = PersonaRepository.findByEmail(email.trim());
        if (user == null) {
            model.addAttribute("error", "El correo no está registrado en el sistema.");
            return "forgot-password";
        }
        
        // Simulación de envío: Aquí se integraría con el EmailService más adelante
        model.addAttribute("mensaje", "Se ha enviado un correo con las instrucciones para restablecer tu contraseña a: " + email);
        return "forgot-password";
    }
}
