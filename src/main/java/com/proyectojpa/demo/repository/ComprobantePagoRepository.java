package com.proyectojpa.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.ComprobantePago;

public interface ComprobantePagoRepository extends JpaRepository<ComprobantePago, Integer> {

    Optional<ComprobantePago> findByOrdenPago_Id(Integer idOrdenPago);

    @Query("SELECT COUNT(c) > 0 FROM ComprobantePago c WHERE c.hashSha256 = :h AND c.ordenPago.estado = :estadoAprobado "
            + "AND c.ordenPago.id <> :idOrden")
    boolean existsMismoArchivoEnOtraOrdenAprobada(@Param("h") String hashSha256,
            @Param("estadoAprobado") String estadoAprobado, @Param("idOrden") Integer idOrdenActual);
}
