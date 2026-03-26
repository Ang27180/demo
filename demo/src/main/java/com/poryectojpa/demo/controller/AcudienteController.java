package com.poryectojpa.demo.controller;

import com.poryectojpa.demo.Service.AcudienteService;
import com.poryectojpa.demo.dto.AcudienteDTO;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/acudiente")
public class AcudienteController {

    private final AcudienteService service;

    public AcudienteController(AcudienteService service) {
        this.service = service;
    }

    @GetMapping
    public List<AcudienteDTO> listar() {
        return service.listar();
    }

    @PostMapping
    public AcudienteDTO guardar(@RequestBody AcudienteDTO dto) {
        return service.guardar(dto);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
