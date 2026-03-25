package com.poryectojpa.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository; // ← ESTA ES LA CORRECTA

import com.poryectojpa.demo.models.Curso;
import com.poryectojpa.demo.models.Estudiante;
import com.poryectojpa.demo.models.Inscripcion;
public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {
    List<Inscripcion> findByEstudiante_IdEstudiante(Integer idEstudiante);

    // Método para buscar una inscripción específica por estudiante y curso
    java.util.Optional<Inscripcion> findByEstudianteAndCurso(Estudiante estudiante, Curso curso);

    boolean existsByEstudianteAndCurso(Estudiante estudiante, Curso curso);
    
    // Buscar todas las inscripciones de los cursos asociados a un tutor específico
    List<Inscripcion> findByCurso_Tutor_IdTutor(Integer idTutor);
}
