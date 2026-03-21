package com.poryectojpa.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.poryectojpa.demo.models.Acudiente;

public interface AcudienteRepository extends JpaRepository<Acudiente, Integer> {
}
