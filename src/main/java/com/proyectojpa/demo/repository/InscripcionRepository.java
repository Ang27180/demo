package com.proyectojpa.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository; // ← ESTA ES LA CORRECTA

import com.proyectojpa.demo.models.Curso;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    List<Inscripcion> findByEstudiante_IdEstudiante(Integer idEstudiante);
    //List<Inscripcion> findByIdEstudiante(Integer estudiante);
    boolean existsByEstudianteAndCurso(Estudiante estudiante, Curso curso);

}
