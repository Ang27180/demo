package com.poryectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poryectojpa.demo.models.EstadoInscripcion;

public interface EstadoInscripcionRepository extends JpaRepository<EstadoInscripcion, Integer> {
    EstadoInscripcion findBynombre(String nombre);
}
