package com.proyectojpa.demo.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.proyectojpa.demo.models.OrdenPago;

public interface OrdenPagoRepository extends JpaRepository<OrdenPago, Integer> {

    @Query("SELECT o FROM OrdenPago o JOIN FETCH o.inscripcion i JOIN FETCH i.estudiante e JOIN FETCH e.persona "
            + "JOIN FETCH i.curso JOIN FETCH i.estado WHERE o.id = :id")
    Optional<OrdenPago> findByIdWithDetalle(@Param("id") Integer id);

    boolean existsByInscripcion_IdAndEstadoIn(Integer idInscripcion, Collection<String> estados);

    Optional<OrdenPago> findFirstByInscripcion_IdAndEstadoInOrderByIdDesc(Integer idInscripcion,
            Collection<String> estados);

    List<OrdenPago> findByEstadoInOrderByFechaCreacionDesc(Collection<String> estados);

    List<OrdenPago> findAllByOrderByFechaCreacionDesc();

    @Query("SELECT o FROM OrdenPago o JOIN FETCH o.inscripcion i JOIN FETCH i.estudiante e JOIN FETCH e.persona "
            + "JOIN FETCH i.curso ORDER BY o.fechaCreacion DESC")
    List<OrdenPago> findAllWithDetalleOrderByFechaCreacionDesc();

    @Query("SELECT o FROM OrdenPago o JOIN FETCH o.inscripcion i JOIN FETCH i.estudiante e JOIN FETCH e.persona "
            + "JOIN FETCH i.curso WHERE o.estado IN :estados ORDER BY o.fechaCreacion DESC")
    List<OrdenPago> findByEstadoInWithDetalleOrderByFechaCreacionDesc(@Param("estados") Collection<String> estados);

    @Query("SELECT o FROM OrdenPago o JOIN FETCH o.inscripcion i WHERE i.estudiante.idEstudiante = :idEst "
            + "AND o.estado IN :estados ORDER BY o.id DESC")
    List<OrdenPago> findActivasPorEstudiante(@Param("idEst") Integer idEstudiante, @Param("estados") Collection<String> estados);

    @Query("SELECT COUNT(o) FROM OrdenPago o WHERE o.inscripcion.estudiante.idEstudiante = :idEst "
            + "AND o.fechaCreacion >= :ini AND o.fechaCreacion < :fin")
    long countByEstudianteAndFechaCreacionBetween(@Param("idEst") Integer idEstudiante,
            @Param("ini") LocalDateTime ini, @Param("fin") LocalDateTime fin);

    @Query("SELECT o FROM OrdenPago o WHERE o.fechaVencimiento IS NOT NULL AND o.fechaVencimiento < :hoy "
            + "AND o.estado IN :estados")
    List<OrdenPago> findVencidas(@Param("hoy") java.time.LocalDate hoy, @Param("estados") Collection<String> estados);
}
