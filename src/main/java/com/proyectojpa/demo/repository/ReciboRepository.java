package com.proyectojpa.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Recibo;

public interface ReciboRepository extends JpaRepository<Recibo, Integer> {

    boolean existsByInscripcion(Inscripcion inscripcion);

    Optional<Recibo> findByInscripcion(Inscripcion inscripcion);

    boolean existsByMedioPagoId(Integer idMedioPago);

    @Query("SELECT r FROM Recibo r "
            + "JOIN FETCH r.inscripcion i "
            + "JOIN FETCH i.curso "
            + "JOIN FETCH i.estudiante e "
            + "JOIN FETCH e.persona "
            + "JOIN FETCH r.medioPago "
            + "WHERE r.id = :id")
    Optional<Recibo> findByIdWithDetalle(@Param("id") Integer id);

    @Query("SELECT DISTINCT r FROM Recibo r "
            + "JOIN FETCH r.inscripcion i "
            + "JOIN FETCH i.curso "
            + "JOIN FETCH i.estudiante e "
            + "JOIN FETCH e.persona "
            + "JOIN FETCH r.medioPago")
    List<Recibo> findAllWithDetalle();
}
