package com.poryectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poryectojpa.demo.models.Tutor;

public interface TutorRepository extends JpaRepository<Tutor, Integer> {

}
