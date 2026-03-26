package com.proyectojpa.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    Optional<Estudiante> findFirstByPersonaOrderByIdEstudianteAsc(Persona persona);

    default Optional<Estudiante> findByPersona(Persona persona) {
        return findFirstByPersonaOrderByIdEstudianteAsc(persona);
    }

    @Query("SELECT e FROM Estudiante e JOIN FETCH e.estadoEstudiante WHERE e.persona = :p ORDER BY e.idEstudiante ASC")
    List<Estudiante> findAllWithEstadoByPersonaOrderById(@Param("p") Persona p);

    default Optional<Estudiante> findByPersonaWithEstado(@Param("p") Persona p) {
        List<Estudiante> list = findAllWithEstadoByPersonaOrderById(p);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}
