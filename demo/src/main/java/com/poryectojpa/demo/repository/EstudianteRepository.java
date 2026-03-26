package com.poryectojpa.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.models.Persona;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
            Optional<Estudiante> findByPersona(Persona persona);
    
}
