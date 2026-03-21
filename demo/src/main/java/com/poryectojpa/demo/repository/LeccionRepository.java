package com.poryectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poryectojpa.demo.models.Leccion;

public interface LeccionRepository extends JpaRepository<Leccion, Integer> {
}
