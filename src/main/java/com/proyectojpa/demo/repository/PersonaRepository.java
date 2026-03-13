package com.proyectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyectojpa.demo.models.Persona;

@Repository
public interface PersonaRepository extends JpaRepository<Persona, Integer> {
    Persona findByEmail(String email);
    Long countByGenero(String genero);
    Long countByRolId(Integer rolId);
}
