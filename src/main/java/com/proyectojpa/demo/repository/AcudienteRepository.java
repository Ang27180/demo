package com.proyectojpa.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Acudiente;

/**
 * AcudienteRepository: Encargado de las operaciones de base de datos para los Acudientes.
 * Se han añadido métodos custom para buscar acudientes propios y del estudiante.
 */
public interface AcudienteRepository extends JpaRepository<Acudiente, Integer> {
    
    // Lista los registros de Acudiente que sean propiedad del usuario autenticado (ID Persona)
    List<Acudiente> findByPersonaId(Integer idPersona);

    @Query("SELECT a FROM Acudiente a "
            + "JOIN FETCH a.persona p "
            + "LEFT JOIN FETCH a.estudianteDependiente e "
            + "LEFT JOIN FETCH e.persona "
            + "WHERE p.id = :idPersona")
    List<Acudiente> findByPersonaIdWithDetalle(@Param("idPersona") Integer idPersona);
    
    // Lista todos los acudientes relacionados a un mismo estudiante
    List<Acudiente> findByEstudianteDependienteIdEstudiante(Integer idEstudiante);

    @Query("SELECT a FROM Acudiente a "
            + "JOIN FETCH a.persona p "
            + "JOIN FETCH a.estudianteDependiente e "
            + "LEFT JOIN FETCH e.persona "
            + "WHERE e.idEstudiante = :idEstudiante")
    List<Acudiente> findByEstudianteDependienteIdEstudianteWithDetalle(@Param("idEstudiante") Integer idEstudiante);
}
