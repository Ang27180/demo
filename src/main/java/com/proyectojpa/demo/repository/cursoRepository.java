package com.proyectojpa.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Curso;

public interface cursoRepository extends JpaRepository<Curso, Integer> {

    // Nota: evitar MultipleBagFetchException. Primero cargamos solo `modulos`
    // y luego inicializamos `modulos.lecciones` en el controlador con otra carga.
    @EntityGraph(attributePaths = { "modulos", "modulos.lecciones" })
    @Query("SELECT c FROM Curso c WHERE c.id = :id")
    Optional<Curso> findByIdWithContenido(@Param("id") Integer id);

    @Query("SELECT c FROM Curso c JOIN FETCH c.tutor t JOIN FETCH t.persona WHERE t.idTutor = :idTutor")
    List<Curso> findByTutor_IdTutorWithTutorPersona(@Param("idTutor") Integer idTutor);

    @Query("SELECT c FROM Curso c JOIN FETCH c.tutor t WHERE c.id = :idCurso AND t.idTutor = :idTutor")
    Optional<Curso> findByIdAndTutor_IdTutor(@Param("idCurso") Integer idCurso, @Param("idTutor") Integer idTutor);

    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.tutor WHERE c.id = :id")
    Optional<Curso> findByIdWithTutor(@Param("id") Integer id);

    /** Listado admin: tutor y persona para mostrar nombre sin LazyInitializationException. */
    @Query("SELECT DISTINCT c FROM Curso c LEFT JOIN FETCH c.tutor t LEFT JOIN FETCH t.persona ORDER BY c.id")
    List<Curso> findAllWithTutorPersonaOrderById();
}
