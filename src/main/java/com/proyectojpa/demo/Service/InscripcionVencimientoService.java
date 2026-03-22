package com.proyectojpa.demo.Service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;

/**
 * Marca como {@link InscripcionEstados#CANCELADA} las inscripciones en
 * {@link InscripcionEstados#PENDIENTE_PAGO} cuyo plazo de pago ha vencido.
 */
@Service
public class InscripcionVencimientoService {

    private static final Logger log = LoggerFactory.getLogger(InscripcionVencimientoService.class);

    private final InscripcionRepository inscripcionRepository;
    private final EstadoInscripcionRepository estadoInscripcionRepository;
    private final EstudianteRepository estudianteRepository;

    public InscripcionVencimientoService(InscripcionRepository inscripcionRepository,
            EstadoInscripcionRepository estadoInscripcionRepository,
            EstudianteRepository estudianteRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.estadoInscripcionRepository = estadoInscripcionRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Scheduled(cron = "${app.inscripcion.vencimiento-cron:0 0 0 * * ?}")
    @Transactional
    public void cancelarInscripcionesPendientesVencidas() {
        LocalDate hoy = LocalDate.now();
        var cancelada = estadoInscripcionRepository.findByCodigo(InscripcionEstados.CANCELADA).orElse(null);
        if (cancelada == null) {
            log.warn("Estado CANCELADA no existe en BD; no se procesan vencimientos.");
            return;
        }
        var inactivo = estadoInscripcionRepository.findByCodigo(InscripcionEstados.INACTIVO).orElse(null);

        var vencidas = inscripcionRepository.findByEstadoCodigoAndFechaLimitePagoBefore(
                InscripcionEstados.PENDIENTE_PAGO, hoy);
        for (Inscripcion i : vencidas) {
            i.setEstado(cancelada);
            log.info("Inscripción id={} marcada CANCELADA por plazo de pago vencido.", i.getId());
            if (inactivo != null && i.getEstudiante() != null) {
                i.getEstudiante().setEstadoEstudiante(inactivo);
                estudianteRepository.save(i.getEstudiante());
                log.info("Cuenta estudiante id={} marcada INACTIVA por incumplimiento de pago.", i.getEstudiante().getIdEstudiante());
            }
        }
    }
}
