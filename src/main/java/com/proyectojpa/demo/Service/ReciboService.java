package com.proyectojpa.demo.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Estudiante;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.MedioPago;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.models.Recibo;
import com.proyectojpa.demo.repository.AcudienteRepository;
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
    private final AcudienteRepository acudienteRepository;
    private final ReciboAutorizacionService reciboAutorizacionService;

    public ReciboService(ReciboRepository reciboRepository,
            MedioPagoRepository medioPagoRepository,
            InscripcionRepository inscripcionRepository,
            EstudianteRepository estudianteRepository,
            EstadoInscripcionRepository estadoInscripcionRepository,
            AcudienteRepository acudienteRepository,
            ReciboAutorizacionService reciboAutorizacionService) {
        this.reciboRepository = reciboRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.estudianteRepository = estudianteRepository;
        this.estadoInscripcionRepository = estadoInscripcionRepository;
        this.acudienteRepository = acudienteRepository;
        this.reciboAutorizacionService = reciboAutorizacionService;
    }

    /** Hay al menos un acudiente vinculado al estudiante (el pago debe gestionarlo el acudiente). */
    @Transactional(readOnly = true)
    public boolean tieneAcudienteVinculado(Estudiante estudiante) {
        if (estudiante == null || estudiante.getIdEstudiante() == null) {
            return false;
        }
        return !acudienteRepository.findByEstudianteDependienteIdEstudiante(estudiante.getIdEstudiante()).isEmpty();
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
        if (tieneAcudienteVinculado(estudiante)) {
            throw new IllegalStateException(
                    "Tu cuenta tiene acudiente registrado. Quien debe generar el recibo de pago es el acudiente. "
                            + "Puedes enviarle un aviso por correo desde esta pantalla.");
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

    /**
     * Genera el recibo iniciando sesión como acudiente vinculado al estudiante de la inscripción.
     */
    @Transactional
    public Recibo generarReciboComoAcudiente(Persona acudientePersona, Integer idInscripcion, Integer idMedioPago) {
        Inscripcion insc = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));
        Estudiante est = insc.getEstudiante();
        if (est == null) {
            throw new IllegalStateException("Inscripción sin estudiante");
        }
        if (!reciboAutorizacionService.esAcudienteDeEstudiante(acudientePersona, est)) {
            throw new IllegalStateException("No está autorizado como acudiente de este estudiante");
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

    /** Igual que {@link #generarRecibo} pero sin comprobar la sesión del estudiante (solo admin). */
    @Transactional
    public Recibo generarReciboComoAdministrador(Integer idInscripcion, Integer idMedioPago) {
        Inscripcion insc = inscripcionRepository.findById(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));
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

    public List<Inscripcion> listarInscripcionesPendientesPagoSinRecibo() {
        return inscripcionRepository.findPendientePagoSinRecibo(InscripcionEstados.PENDIENTE_PAGO);
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
