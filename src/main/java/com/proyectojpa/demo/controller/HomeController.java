package com.proyectojpa.demo.controller;

import com.proyectojpa.demo.repository.cursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private cursoRepository cursoRepository;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("titulo", "Inicio - Sabor MasterClass");
        model.addAttribute("cursos", cursoRepository.findAll());
        return "home";
    }
}
