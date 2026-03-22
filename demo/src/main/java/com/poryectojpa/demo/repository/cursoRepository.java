package com.poryectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poryectojpa.demo.models.Curso;
import com.poryectojpa.demo.models.Tutor;
import java.util.List;

public interface cursoRepository extends JpaRepository<Curso, Integer> {
    List<Curso> findByTutor(Tutor tutor);
}