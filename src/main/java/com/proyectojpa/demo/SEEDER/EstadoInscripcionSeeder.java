package com.proyectojpa.demo.SEEDER;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.EstadoInscripcion;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;

@Component
public class EstadoInscripcionSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(EstadoInscripcionSeeder.class);

    private final EstadoInscripcionRepository estadoInscripcionRepository;

    public EstadoInscripcionSeeder(EstadoInscripcionRepository estadoInscripcionRepository) {
        this.estadoInscripcionRepository = estadoInscripcionRepository;
    }

    @Override
    public void run(String... args) {
        for (EstadoInscripcion e : estadoInscripcionRepository.findAll()) {
            if (e.getCodigo() == null || e.getCodigo().isBlank()) {
                if (e.getId() != null && e.getId() == 1) {
                    e.setCodigo(InscripcionEstados.ACTIVO);
                } else if (esPendienteLegacy(e)
                        && !estadoInscripcionRepository.existsByCodigo(InscripcionEstados.PENDIENTE_PAGO)) {
                    e.setCodigo(InscripcionEstados.PENDIENTE_PAGO);
                } else {
                    e.setCodigo("LEGACY_" + e.getId());
                }
                estadoInscripcionRepository.save(e);
                log.info("Estado id={} asignado codigo={}", e.getId(), e.getCodigo());
            }
        }

        for (EstadoInscripcion e : estadoInscripcionRepository.findAll()) {
            String c = e.getCodigo();
            if (c != null && c.startsWith("LEGACY_") && esPendienteLegacy(e)
                    && !estadoInscripcionRepository.existsByCodigo(InscripcionEstados.PENDIENTE_PAGO)) {
                e.setCodigo(InscripcionEstados.PENDIENTE_PAGO);
                estadoInscripcionRepository.save(e);
                log.info("Estado id={} remapeado de LEGACY a {}", e.getId(), InscripcionEstados.PENDIENTE_PAGO);
            }
        }

        asegurar(InscripcionEstados.ACTIVO, "Activo (acceso al curso)");
        asegurar(InscripcionEstados.PENDIENTE_PAGO, "Pendiente de pago");
    }

    /** Heurística para bases antiguas: id=2 o nombre con "pendiente". */
    private static boolean esPendienteLegacy(EstadoInscripcion e) {
        if (e.getId() != null && e.getId() == 2) {
            return true;
        }
        if (e.getNombre() == null) {
            return false;
        }
        return e.getNombre().toLowerCase().contains("pendiente");
    }

    private void asegurar(String codigo, String nombre) {
        if (!estadoInscripcionRepository.existsByCodigo(codigo)) {
            EstadoInscripcion e = new EstadoInscripcion();
            e.setCodigo(codigo);
            e.setNombre(nombre);
            estadoInscripcionRepository.save(e);
            log.info("Estado de inscripción creado: {} — {}", codigo, nombre);
        }
    }
}
