package com.proyectojpa.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {
            Optional<Estudiante> findByPersona(Persona persona);
    
}
