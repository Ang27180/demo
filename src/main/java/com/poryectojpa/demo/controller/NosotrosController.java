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
public class NosotrosController {

    @GetMapping("/nosotros")
    public String mostrarNosotros(Model model) {
        // Este atributo es opcional, solo para que tu layout muestre un título dinámico
        model.addAttribute("titulo", "Nosotros - Sabor MasterClass");

        // Retorna el nombre del template (sin .html)
        return "nosotros";
    }
}