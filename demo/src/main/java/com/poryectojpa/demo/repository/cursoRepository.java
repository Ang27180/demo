package com.poryectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poryectojpa.demo.models.Curso;

public interface cursoRepository extends JpaRepository<Curso, Integer> {
}