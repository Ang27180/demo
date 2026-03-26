package com.poryectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.poryectojpa.demo.models.Persona;

@Repository
public interface personaRepository extends JpaRepository<Persona, Integer> {
    Persona findByEmail(String email);
    Long countByGenero(String genero);
    Long countByRolId(Integer rolId);
}
