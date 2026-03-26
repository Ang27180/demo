package com.proyectojpa.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {

    Optional<Tutor> findByPersona(Persona persona);

    // Solo tutores asociados a personas con rolId=3 (TUTOR)
    @Query("SELECT DISTINCT t FROM Tutor t JOIN FETCH t.persona WHERE t.persona.rolId = 3 ORDER BY t.idTutor")
    List<Tutor> findAllWithPersona();

    @Query("SELECT t FROM Tutor t JOIN FETCH t.persona WHERE t.idTutor = :id")
    Optional<Tutor> findByIdWithPersona(@Param("id") Integer id);
}
