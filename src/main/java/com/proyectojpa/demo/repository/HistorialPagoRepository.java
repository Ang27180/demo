package com.proyectojpa.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyectojpa.demo.models.HistorialPago;

public interface HistorialPagoRepository extends JpaRepository<HistorialPago, Integer> {

    List<HistorialPago> findByOrdenPago_IdOrderByFechaAsc(Integer idOrdenPago);
}
