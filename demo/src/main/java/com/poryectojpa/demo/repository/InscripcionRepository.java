package com.poryectojpa.demo.repository;

import java.util.List;  // ← ESTA ES LA CORRECTA

import org.springframework.data.jpa.repository.JpaRepository;
import com.poryectojpa.demo.models.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    List<Inscripcion> findByIdEstudiante(Integer idEstudiante);

}
