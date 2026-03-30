package com.proyectojpa.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.proyectojpa.demo.models.Tutor;
import com.proyectojpa.demo.repository.TutorRepository;
import com.proyectojpa.demo.repository.cursoRepository;

@Controller
public class NosotrosController {

    private final TutorRepository tutorRepository;
    private final cursoRepository cursoRepository;

    public NosotrosController(TutorRepository tutorRepository, cursoRepository cursoRepository) {
        this.tutorRepository = tutorRepository;
        this.cursoRepository = cursoRepository;
    }

    @GetMapping({ "/nosotros", "/nuestros-tutores" })
    public String mostrarNosotros(Model model) {
        model.addAttribute("titulo", "Nosotros - Sabor MasterClass");

        List<Tutor> tutores = tutorRepository.findAllWithPersona();
        Map<Integer, Long> cursosPorTutor = new HashMap<>();
        for (Tutor t : tutores) {
            cursosPorTutor.put(t.getIdTutor(), cursoRepository.countByTutor_IdTutor(t.getIdTutor()));
        }
        model.addAttribute("tutores", tutores);
        model.addAttribute("cursosPorTutor", cursosPorTutor);

        return "nosotros";
    }
}
