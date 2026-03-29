package com.proyectojpa.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyectojpa.demo.models.Acudiente;

/**
 * AcudienteRepository: Encargado de las operaciones de base de datos para los Acudientes.
 * Se han añadido métodos custom para buscar acudientes propios y del estudiante.
 */
public interface AcudienteRepository extends JpaRepository<Acudiente, Integer> {
    
    // Lista los registros de Acudiente que sean propiedad del usuario autenticado (ID Persona)
    List<Acudiente> findByPersonaId(Integer idPersona);
    
    // Lista todos los acudientes relacionados a un mismo estudiante
    List<Acudiente> findByEstudianteDependienteIdEstudiante(Integer idEstudiante);
}
