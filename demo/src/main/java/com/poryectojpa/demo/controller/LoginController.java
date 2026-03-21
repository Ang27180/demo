package com.poryectojpa.demo.controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.poryectojpa.demo.dto.LoginDTO;
import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.personaRepository;
import jakarta.validation.Valid;

@Controller
public class LoginController {

    @Autowired
    private personaRepository personaRepository;

    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("login", new LoginDTO());
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @Valid @ModelAttribute("login") LoginDTO loginDTO,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "login";
        }

        Persona user = personaRepository.findByEmail(loginDTO.getEmail().trim());
        if (user == null) {
            model.addAttribute("mensaje", "Correo o contraseña incorrectos");
            return "login";
        }

        if (!user.getContrasena().equals(loginDTO.getContrasena())) {
            model.addAttribute("mensaje", "Correo o contraseña incorrectos");
            return "login";
        }

        if (user.getRolId() != null && user.getRolId() == 1) {
            return "redirect:/admin";
        } else {
            return "redirect:/estudiante";
        }
    }
    @GetMapping("/forgot-password")
    public String mostrarRecuperarContrasena() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String procesarRecuperarContrasena(@RequestParam String email, Model model) {
        Persona user = personaRepository.findByEmail(email.trim());
        if (user == null) {
            model.addAttribute("error", "El correo no está registrado en el sistema.");
            return "forgot-password";
        }
        
        // Simulación de envío: Aquí se integraría con el EmailService más adelante
        model.addAttribute("mensaje", "Se ha enviado un correo con las instrucciones para restablecer tu contraseña a: " + email);
        return "forgot-password";
    }
}
