package com.proyectojpa.demo.repository;

import java.time.LocalDate;
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

    @Query("SELECT i FROM Inscripcion i "
            + "JOIN FETCH i.estudiante e "
            + "JOIN FETCH e.persona "
            + "JOIN FETCH i.curso "
            + "JOIN FETCH i.estado "
            + "WHERE i.id = :id")
    Optional<Inscripcion> findByIdForCertificado(@Param("id") Integer id);

    @Query("SELECT DISTINCT i FROM Inscripcion i JOIN FETCH i.estado JOIN FETCH i.estudiante e "
            + "WHERE i.estado.codigo = :codigo "
            + "AND i.fechaLimitePago IS NOT NULL AND i.fechaLimitePago < :hoy")
    List<Inscripcion> findByEstadoCodigoAndFechaLimitePagoBefore(@Param("codigo") String codigo,
            @Param("hoy") LocalDate hoy);

    @Query("SELECT DISTINCT i FROM Inscripcion i "
            + "JOIN FETCH i.estudiante e "
            + "JOIN FETCH e.persona "
            + "JOIN FETCH i.curso c "
            + "JOIN FETCH i.estado "
            + "WHERE c.id = :idCurso")
    List<Inscripcion> findByCursoIdWithEstudianteAndEstado(@Param("idCurso") Integer idCurso);
}
