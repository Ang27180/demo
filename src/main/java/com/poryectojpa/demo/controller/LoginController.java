package com.poryectojpa.demo.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.personaRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    @Autowired
    private personaRepository personaRepository;

    // GET: mostrar login
    @GetMapping("/login")
    public String mostrarLogin(Model model) {
        model.addAttribute("loginForm", new Persona()); // usamos Persona solo para correo/password
        return "login";
    }

    // POST: procesar login
    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute("loginForm") Persona persona, 
                                HttpSession session, Model model) {
        Persona usuario = personaRepository.findByEmail(persona.getEmail());

        if (usuario != null && usuario.getContraseña().equals(persona.getContraseña())) {
            // guardar datos en sesión
            session.setAttribute("usuario", usuario.getEmail());
            session.setAttribute("rol", usuario.getRolId());

            // redirigir según rol
            switch (usuario.getRolId()) {
                case 1: return "redirect:/administrador";
                case 2: return "redirect:/estudiante";
                case 3: return "redirect:/vendedor";
                default: return "redirect:/";
            }
        } else {
            model.addAttribute("mensaje", "Usuario o contraseña incorrecta");
            return "login";
        }
    }

    // cerrar sesión
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
