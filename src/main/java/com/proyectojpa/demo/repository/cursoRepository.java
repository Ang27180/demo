package com.proyectojpa.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Curso;

public interface cursoRepository extends JpaRepository<Curso, Integer> {

    @EntityGraph(attributePaths = { "modulos", "modulos.lecciones" })
    @Query("SELECT c FROM Curso c WHERE c.id = :id")
    Optional<Curso> findByIdWithContenido(@Param("id") Integer id);
}
