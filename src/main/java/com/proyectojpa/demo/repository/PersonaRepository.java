package com.proyectojpa.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyectojpa.demo.models.Persona;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Persona findByEmail(String email);

    /** Búsqueda insensible a mayúsculas (login / registro). */
    Optional<Persona> findByEmailIgnoreCase(String email);

    Long countByGenero(String genero);
    Long countByRolId(Integer rolId);

    @Query("SELECT DISTINCT p FROM Persona p LEFT JOIN FETCH p.estudiante e LEFT JOIN FETCH e.estadoEstudiante")
    List<Persona> findAllWithEstudianteEstado();
}
