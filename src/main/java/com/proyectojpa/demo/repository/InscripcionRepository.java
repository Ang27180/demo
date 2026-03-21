package com.proyectojpa.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    List<Inscripcion> findByEstudiante_IdEstudiante(Integer idEstudiante);

    @Query("SELECT DISTINCT i FROM Inscripcion i "
            + "JOIN FETCH i.curso "
            + "JOIN FETCH i.estado "
            + "WHERE i.estudiante.idEstudiante = :idEst")
    List<Inscripcion> findByEstudianteIdWithCursoAndEstado(@Param("idEst") Integer idEstudiante);

    boolean existsByEstudianteAndCurso(Estudiante estudiante, Curso curso);

    Optional<Inscripcion> findByEstudianteAndCurso(Estudiante estudiante, Curso curso);

    @Query("SELECT i FROM Inscripcion i JOIN FETCH i.estado WHERE i.estudiante = :est AND i.curso = :cur")
    Optional<Inscripcion> findByEstudianteAndCursoWithEstado(@Param("est") Estudiante estudiante,
            @Param("cur") Curso curso);

    @Query("SELECT i FROM Inscripcion i "
            + "JOIN FETCH i.estudiante e "
            + "JOIN FETCH e.persona "
            + "JOIN FETCH i.curso "
            + "WHERE i.id = :id")
    Optional<Inscripcion> findByIdWithEstudiantePersonaAndCurso(@Param("id") Integer id);
}
