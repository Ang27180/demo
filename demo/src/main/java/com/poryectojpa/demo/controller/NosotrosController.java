package com.poryectojpa.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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