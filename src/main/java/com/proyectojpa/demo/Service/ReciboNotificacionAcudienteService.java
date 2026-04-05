package com.proyectojpa.demo.Service;

import java.util.List;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyectojpa.demo.models.Acudiente;
import com.proyectojpa.demo.models.Inscripcion;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.AcudienteRepository;
import com.proyectojpa.demo.repository.InscripcionRepository;

import jakarta.mail.MessagingException;

/**
 * Envía correo al(los) acudiente(s) para que inicien sesión y generen el recibo
 * u orden de pago.
 */
@Service
public class ReciboNotificacionAcudienteService {

    private final InscripcionRepository inscripcionRepository;
    private final AcudienteRepository acudienteRepository;
    private final ReciboService reciboService;
    private final EmailService emailService;

    @Value("${app.public-url:http://localhost:8080}")
    private String publicUrl;

    public ReciboNotificacionAcudienteService(InscripcionRepository inscripcionRepository,
            AcudienteRepository acudienteRepository,
            ReciboService reciboService,
            EmailService emailService) {
        this.inscripcionRepository = inscripcionRepository;
        this.acudienteRepository = acudienteRepository;
        this.reciboService = reciboService;
        this.emailService = emailService;
    }

    /**
     * Valida que la persona sea el estudiante titular, que la inscripción sea suya
     * y que tenga acudiente;
     * envía un correo HTML a cada acudiente con correo configurado.
     */
    @Transactional
    public int enviarSolicitudPagoAAcudientes(Persona estudiantePersona, Integer idInscripcion)
            throws MessagingException {
        Inscripcion insc = inscripcionRepository.findByIdWithEstudiantePersonaAndCurso(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));
        if (!insc.getEstudiante().getPersona().getId().equals(estudiantePersona.getId())) {
            throw new IllegalStateException("La inscripción no pertenece al estudiante");
        }
        if (!reciboService.tieneAcudienteVinculado(insc.getEstudiante())) {
            throw new IllegalStateException("No hay acudiente registrado para este estudiante");
        }
        List<Acudiente> vinculos = acudienteRepository
                .findByEstudianteDependienteIdEstudianteWithDetalle(insc.getEstudiante().getIdEstudiante());
        String nombreEst = insc.getEstudiante().getPersona().getNombre();
        String nombreCurso = insc.getCurso() != null ? insc.getCurso().getNombre() : "el curso";
        String base = EmailLayoutInstitucional.baseUrlSinSlashFinal(publicUrl);
        String loginUrl = base + "/login";
        String panelUrl = base + "/acudiente/panel";
        String panelReciboUrl = base + "/acudiente/pagos/recibo/nuevo/" + idInscripcion;

        int enviados = 0;
        for (Acudiente v : vinculos) {
            Persona ap = v.getPersona();
            if (ap == null || ap.getEmail() == null || ap.getEmail().isBlank()) {
                continue;
            }
            String html = cuerpoHtml(nombreEst, nombreCurso, loginUrl, panelUrl, panelReciboUrl);
            emailService.enviarHtml(ap.getEmail().trim(),
                    "Pago pendiente — inscripción de " + nombreEst + " (Sabor MasterClass)",
                    html);
            enviados++;
        }
        if (enviados == 0) {
            throw new IllegalStateException(
                    "Ningún acudiente tiene correo registrado. Actualiza el correo del acudiente en el perfil.");
        }
        return enviados;
    }

    /**
     * Primera visita del estudiante a la pantalla de recibo (con acudiente): correo
     * al acudiente y marca fecha.
     */
    @Transactional
    public void notificarPrimeraVisitaReciboSiCorresponde(Persona estudiantePersona, Integer idInscripcion)
            throws MessagingException {
        Inscripcion insc = inscripcionRepository.findByIdWithEstudiantePersonaAndCurso(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));
        if (!insc.getEstudiante().getPersona().getId().equals(estudiantePersona.getId())) {
            throw new IllegalStateException("La inscripción no pertenece al estudiante");
        }
        if (!reciboService.tieneAcudienteVinculado(insc.getEstudiante())) {
            return;
        }
        if (insc.getFechaSolicitudReciboAcudiente() != null) {
            return;
        }
        int n = enviarSolicitudPagoAAcudientes(estudiantePersona, idInscripcion);
        if (n > 0) {
            insc.setFechaSolicitudReciboAcudiente(LocalDateTime.now());
            inscripcionRepository.save(insc);
        }
    }

    private String cuerpoHtml(String nombreEstudiante, String nombreCurso, String loginUrl,
            String panelAcudienteUrl, String urlReciboAcudiente) {
        String inner = """
                <p style="color: #555555; font-size: 16px; line-height: 1.6; margin-bottom: 25px;">
                    El estudiante <b>%s</b> se inscribió en <b>%s</b> y el pago está <b>pendiente</b>. &#128181;
                </p>

                <div style="background-color: #f9f9f9; padding: 25px; border-radius: 8px; border-left: 5px solid #6619f5; color: #444444; font-size: 16px; line-height: 1.6;">
                    <p style="margin-top: 0;">
                        Como acudiente, <b>inicia sesión</b> y genera el recibo de pago desde tu panel de acudiente.
                        &#128187; El estudiante no puede generar el recibo desde su propia cuenta.
                    </p>
                    <p style="margin-bottom: 10px;">
                        &#128273; Inicia sesión con tu correo y la contraseña de tu cuenta de acudiente
                        (<a href="%s" style="color: #6619f5;">ir al inicio de sesión</a>).
                    </p>
                    <p style="margin-bottom: 0;">
                        &#128203; Puedes abrir el panel y el recibo directamente:
                        <a href="%s" style="color: #6619f5;">panel de acudiente</a>
                        ·
                        <a href="%s" style="color: #6619f5;">generar recibo de esta inscripción</a>.
                    </p>
                </div>
                """
                .formatted(nombreEstudiante, nombreCurso, loginUrl, panelAcudienteUrl, urlReciboAcudiente);
        return EmailLayoutInstitucional.pagina("&#9203; Pago pendiente", inner, "IR AL PANEL DE ACUDIENTE",
                panelAcudienteUrl);
    }
}
