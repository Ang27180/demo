package com.proyectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectojpa.demo.models.Leccion;

public interface LeccionRepository extends JpaRepository<Leccion, Integer> {
}
