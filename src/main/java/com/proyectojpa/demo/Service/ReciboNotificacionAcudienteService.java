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
        String base = publicUrl.replaceAll("/$", "");
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
        return """
                        <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f4f4f4; font-family: 'Segoe UI', Arial, sans-serif;">
                    <table align="center" border="0" cellpadding="0" cellspacing="0" width="100%%" style="max-width: 600px; background-color: #ffffff; border-radius: 12px; overflow: hidden; margin-top: 30px; margin-bottom: 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.1);">

                        <tr>
                            <td align="center" style="background-color: #000000; padding: 40px 20px; border-bottom: 4px solid #6619f5;">
                                <h1 style="color: #ffffff; margin: 0; font-size: 28px; letter-spacing: 1px;">Sabor MasterClass</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding: 40px 30px;">
                                <h2 style="color: #333333; margin-top: 0; font-size: 22px;">&#9203; Pago pendiente</h2>

                                <p style="color: #555555; font-size: 16px; line-height: 1.6; margin-bottom: 25px;">
                                    El estudiante <b>%s</b> se inscribió en <b>%s</b> y el pago está <b>pendiente</b>. &#128181;
                                </p>

                                <div style="background-color: #f9f9f9; padding: 25px; border-radius: 8px; border-left: 5px solid #6619f5; color: #444444; font-size: 16px; line-height: 1.6;">
                                    <p style="margin-top: 0;">
                                        Como acudiente, <b>inicia sesión</b> y genera el recibo de pago desde tu panel de acudiente.
                                        &#128187; El estudiante no puede generar el recibo desde su propia cuenta.
                                    </p>
                                    <p style="margin-bottom: 10px;">
                                        &#128273; Inicia sesión con tu correo y la contraseña de tu cuenta de acudiente.
                                    </p>
                                    <p style="margin-bottom: 0;">
                                        &#128203; En el panel de acudiente encontrarás la sección de recibos y el enlace directo.
                                    </p>
                                </div>

                                <p style="color: #333333; font-weight: bold; font-size: 17px; text-align: center; margin-top: 30px; margin-bottom: 10px;">
                                    &#127859; ¡¡Sabor Master Class sabemos lo que hacemos!!
                                </p>

                                <table align="center" border="0" cellpadding="0" cellspacing="0" style="margin-top: 20px;">
                                    <tr>
                                        <td align="center" style="border-radius: 50px; background-color: #000000;">
                                            <a href="http://localhost:8080/home" style="display: inline-block; padding: 12px 35px; color: #ffffff; text-decoration: none; font-weight: bold; border-radius: 50px; font-size: 14px;">
                                                IR AL PANEL DE ACUDIENTE
                                            </a>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>

                        <tr>
                            <td align="center" style="background-color: #000000; padding: 30px 20px; color: #aaaaaa; font-size: 12px;">
                                <p style="margin: 0; color: #ffffff;">Sabor MasterClass © 2025</p>
                                <p style="margin: 5px 0 0 0;">Todos los derechos reservados. Desarrollado por HAMN</p>
                                <div style="margin-top: 15px;">
                                    <span style="color: #6619f5;">&#8226;</span> Facebook <span style="color: #6619f5;">&#8226;</span> Instagram <span style="color: #6619f5;">&#8226;</span> Twitter
                                </div>
                            </td>
                        </tr>
                    </table>
                </body>
                </html> """
                .formatted(nombreEstudiante, nombreCurso, panelAcudienteUrl, loginUrl, panelAcudienteUrl,
                        urlReciboAcudiente);
    }
}
