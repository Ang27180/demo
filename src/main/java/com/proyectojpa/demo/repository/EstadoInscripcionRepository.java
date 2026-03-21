package com.proyectojpa.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyectojpa.demo.models.EstadoInscripcion;

public interface EstadoInscripcionRepository extends JpaRepository<EstadoInscripcion, Integer> {

    Optional<EstadoInscripcion> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
