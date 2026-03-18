package com.proyectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyectojpa.demo.models.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {

}
