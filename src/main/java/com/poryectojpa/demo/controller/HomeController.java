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
public class HomeController {

    @GetMapping({"/home"})
    public String home(Model model) {
        model.addAttribute("titulo", "Inicio - Sabor MasterClass");
        return "home";
    }
}