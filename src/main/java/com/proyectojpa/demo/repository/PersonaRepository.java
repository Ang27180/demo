package com.proyectojpa.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyectojpa.demo.models.Persona;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Persona findByEmail(String email);
    Persona findByDocumento(String documento);

    /** Búsqueda insensible a mayúsculas (login / registro). */
    Optional<Persona> findByEmailIgnoreCase(String email);

    Long countByGenero(String genero);
    Long countByRolId(Integer rolId);

    /** Personas con rol tutor (3), para selectores de cursos sin exigir fila previa en {@code tutor}. */
    List<Persona> findByRolIdOrderByNombreAsc(Integer rolId);

    /** Paginación del listado de supervisión (panel tutor admin). */
    Page<Persona> findByRolIdOrderByNombreAsc(Integer rolId, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Persona p LEFT JOIN FETCH p.estudiantes e LEFT JOIN FETCH e.estadoEstudiante")
    List<Persona> findAllWithEstudianteEstado();

    /**
     * Lista completa para el panel admin con colecciones necesarias para la vista (open-in-view=false).
     */
    @EntityGraph(attributePaths = { "estudiantes", "estudiantes.estadoEstudiante" })
    List<Persona> findAllByOrderByIdAsc();
}
