package com.proyectojpa.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.proyectojpa.demo.models.MedioPago;

public interface MedioPagoRepository extends JpaRepository<MedioPago, Integer> {

    @Query("SELECT DISTINCT m FROM MedioPago m LEFT JOIN FETCH m.adminPersona")
    List<MedioPago> findAllWithAdmin();
}
