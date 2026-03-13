package com.proyectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectojpa.demo.models.EstadoInscripcion;

public interface EstadoInscripcionRepository extends JpaRepository<EstadoInscripcion, Integer> {
    EstadoInscripcion findBynombre(String nombre);
}
