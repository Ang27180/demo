package com.proyectojpa.demo.Service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.MedioPago;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.EstadoInscripcionRepository;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;
import com.proyectojpa.demo.repository.MedioPagoRepository;
import com.proyectojpa.demo.repository.ReciboRepository;

@Service
public class ReciboService {

    public static final String RECIBO_PENDIENTE = "PENDIENTE";
    public static final String RECIBO_PAGADO = "PAGADO";

    private final ReciboRepository reciboRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final EstudianteRepository estudianteRepository;
    private final EstadoInscripcionRepository estadoInscripcionRepository;

    public ReciboService(ReciboRepository reciboRepository,
            MedioPagoRepository medioPagoRepository,
            InscripcionRepository inscripcionRepository,
            EstudianteRepository estudianteRepository,
            EstadoInscripcionRepository estadoInscripcionRepository) {
        this.reciboRepository = reciboRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.estudianteRepository = estudianteRepository;
        this.estadoInscripcionRepository = estadoInscripcionRepository;
    }

    @Transactional
    public Recibo generarRecibo(Persona estudiantePersona, Integer idInscripcion, Integer idMedioPago) {
        Estudiante estudiante = estudianteRepository.findByPersona(estudiantePersona)
                .orElseThrow(() -> new IllegalStateException("No hay perfil de estudiante"));

        Inscripcion insc = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));

        if (!estudiante.getIdEstudiante().equals(insc.getEstudiante().getIdEstudiante())) {
            throw new IllegalStateException("La inscripción no pertenece al estudiante");
        }
        if (insc.getEstado() == null || !InscripcionEstados.PENDIENTE_PAGO.equals(insc.getEstado().getCodigo())) {
            throw new IllegalStateException("Solo se genera recibo con inscripción pendiente de pago");
        }
        if (reciboRepository.existsByInscripcion(insc)) {
            throw new IllegalStateException("Ya existe un recibo para esta inscripción");
        }

        MedioPago medio = medioPagoRepository.findById(idMedioPago)
                .orElseThrow(() -> new IllegalArgumentException("Medio de pago no encontrado"));

        Recibo r = new Recibo();
        r.setInscripcion(insc);
        r.setMedioPago(medio);
        r.setFechaEmision(LocalDate.now());
        r.setEstado(RECIBO_PENDIENTE);
        r.setCodigoQrUnico("REC-" + insc.getId() + "-" + UUID.randomUUID());

        return reciboRepository.save(r);
    }

    @Transactional
    public void marcarReciboPagadoYActivarInscripcion(Integer idRecibo) {
        Recibo r = reciboRepository.findById(idRecibo)
                .orElseThrow(() -> new IllegalArgumentException("Recibo no encontrado"));
        if (RECIBO_PAGADO.equals(r.getEstado())) {
            return;
        }
        r.setEstado(RECIBO_PAGADO);
        reciboRepository.save(r);

        var activo = estadoInscripcionRepository.findByCodigo(InscripcionEstados.ACTIVO)
                .orElseThrow(() -> new IllegalStateException("Estado ACTIVO no configurado en BD"));
        Inscripcion insc = r.getInscripcion();
        insc.setEstado(activo);
        inscripcionRepository.save(insc);
    }
}
