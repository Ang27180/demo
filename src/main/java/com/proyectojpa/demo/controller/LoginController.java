package com.proyectojpa.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyectojpa.demo.dto.LoginDTO;

@Controller
public class LoginController {

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
    // Recuperación de contraseña: ForgotPasswordController (/forgot-password).
}
