package com.poryectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poryectojpa.demo.models.Tutor;
import com.poryectojpa.demo.models.Persona;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {
    Optional<Tutor> findByPersona(Persona persona);
}
