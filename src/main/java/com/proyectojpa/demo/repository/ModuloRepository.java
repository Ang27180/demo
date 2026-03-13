package com.proyectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyectojpa.demo.models.Modulo;

public interface ModuloRepository extends JpaRepository<Modulo, Integer> {
}
