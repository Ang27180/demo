package com.poryectojpa.demo.controller;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poryectojpa.demo.Service.TutorService;
import com.poryectojpa.demo.dto.TutorDTO;

@RestController
@RequestMapping("/tutor")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping
    public List<TutorDTO> listar() {
        return tutorService.listar();
    }

    @PostMapping
    public TutorDTO guardar(@RequestBody TutorDTO dto) {
        return tutorService.guardar(dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        tutorService.eliminar(id);
    }
}

