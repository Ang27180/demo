package com.proyectojpa.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Persona;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    Optional<Estudiante> findByPersona(Persona persona);

    @Query("SELECT e FROM Estudiante e JOIN FETCH e.estadoEstudiante WHERE e.persona = :p")
    Optional<Estudiante> findByPersonaWithEstado(@Param("p") Persona persona);
}
