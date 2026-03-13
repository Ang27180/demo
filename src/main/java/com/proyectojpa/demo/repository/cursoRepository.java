package com.proyectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyectojpa.demo.models.Curso;

public interface cursoRepository extends JpaRepository<Curso, Integer> {
}
