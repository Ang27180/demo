package com.proyectojpa.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.ProgresoLeccion;

public interface ProgresoLeccionRepository extends JpaRepository<ProgresoLeccion, Integer> {

    void deleteByEstudiante_IdEstudiante(Integer idEstudiante);

    boolean existsByEstudiante_IdEstudianteAndLeccion_Id(Integer idEstudiante, Integer idLeccion);

    long countByEstudiante_IdEstudianteAndLeccion_Modulo_Curso_Id(Integer idEstudiante, Integer idCurso);

    @Query("SELECT p.leccion.id FROM ProgresoLeccion p WHERE p.estudiante.idEstudiante = :eid AND p.leccion.modulo.curso.id = :cid")
    List<Integer> findLeccionIdsCompletadas(@Param("eid") Integer idEstudiante, @Param("cid") Integer idCurso);
}
