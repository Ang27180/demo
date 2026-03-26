package com.proyectojpa.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Modulo;

public interface ModuloRepository extends JpaRepository<Modulo, Integer> {

    @Query("SELECT m.curso.id FROM Modulo m WHERE m.id = :id")
    Optional<Integer> findCursoIdByModuloId(@Param("id") Integer moduloId);
}
