package com.proyectojpa.demo.Service;

import java.util.List;

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
 * Envía correo al(los) acudiente(s) para que inicien sesión y generen el recibo u orden de pago.
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
     * Valida que la persona sea el estudiante titular, que la inscripción sea suya y que tenga acudiente;
     * envía un correo HTML a cada acudiente con correo configurado.
     */
    @Transactional(readOnly = true)
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
                .findByEstudianteDependienteIdEstudiante(insc.getEstudiante().getIdEstudiante());
        String nombreEst = insc.getEstudiante().getPersona().getNombre();
        String nombreCurso = insc.getCurso() != null ? insc.getCurso().getNombre() : "el curso";
        String base = publicUrl.replaceAll("/$", "");
        String loginUrl = base + "/login";
        String panelReciboUrl = base + "/acudiente/pagos/recibo/nuevo/" + idInscripcion;

        int enviados = 0;
        for (Acudiente v : vinculos) {
            Persona ap = v.getPersona();
            if (ap == null || ap.getEmail() == null || ap.getEmail().isBlank()) {
                continue;
            }
            String html = cuerpoHtml(nombreEst, nombreCurso, loginUrl, panelReciboUrl);
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

    private String cuerpoHtml(String nombreEstudiante, String nombreCurso, String loginUrl,
            String urlReciboAcudiente) {
        return """
                <div style="font-family:Segoe UI,sans-serif;max-width:560px;line-height:1.5;color:#222;">
                  <h2 style="color:#111;">Pago pendiente</h2>
                  <p>El estudiante <strong>%s</strong> se inscribió en <strong>%s</strong> y el pago está <strong>pendiente</strong>.</p>
                  <p>Como acudiente, debe <strong>iniciar sesión</strong> y <strong>generar el recibo de pago</strong> (código QR / PDF). El estudiante no puede generar el recibo desde su propia cuenta.</p>
                  <ol>
                    <li><a href="%s">Iniciar sesión</a> con el correo y la contraseña de su cuenta de acudiente.</li>
                    <li>Tras entrar, abra: <a href="%s">Generar recibo de pago para esta inscripción</a>.</li>
                  </ol>
                  <p style="font-size:0.9em;color:#555;">Desde el mismo panel de acudiente puede revisar el estado del pago. Si necesita pago por Nequi (orden), contacte al soporte o use el recibo QR según las indicaciones del curso.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:1.5rem 0;">
                  <p style="font-size:0.85em;color:#888;">Sabor MasterClass</p>
                </div>
                """
                .formatted(nombreEstudiante, nombreCurso, loginUrl, urlReciboAcudiente);
    }
}
