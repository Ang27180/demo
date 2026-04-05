package com.proyectojpa.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Leccion;

public interface LeccionRepository extends JpaRepository<Leccion, Integer> {

    long countByModulo_Curso_Id(Integer idCurso);

    long countByModulo_Id(Integer idModulo);

    @Query("SELECT m.curso.id FROM Leccion l JOIN l.modulo m WHERE l.id = :id")
    Optional<Integer> findCursoIdByLeccionId(@Param("id") Integer leccionId);
}
