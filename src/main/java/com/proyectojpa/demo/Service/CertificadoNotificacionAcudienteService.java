package com.proyectojpa.demo.Service;

import java.time.LocalDateTime;
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
 * Notifica por correo al acudiente cuando el estudiante solicita autorización del certificado.
 */
@Service
public class CertificadoNotificacionAcudienteService {

    private final InscripcionRepository inscripcionRepository;
    private final AcudienteRepository acudienteRepository;
    private final ReciboService reciboService;
    private final EmailService emailService;

    @Value("${app.public-url:http://localhost:8080}")
    private String publicUrl;

    public CertificadoNotificacionAcudienteService(InscripcionRepository inscripcionRepository,
            AcudienteRepository acudienteRepository,
            ReciboService reciboService,
            EmailService emailService) {
        this.inscripcionRepository = inscripcionRepository;
        this.acudienteRepository = acudienteRepository;
        this.reciboService = reciboService;
        this.emailService = emailService;
    }

    @Transactional
    public void solicitarAutorizacionCertificado(Persona estudiantePersona, Integer idInscripcion)
            throws MessagingException {
        Inscripcion insc = inscripcionRepository.findByIdWithEstudiantePersonaAndCurso(idInscripcion)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada"));
        if (!insc.getEstudiante().getPersona().getId().equals(estudiantePersona.getId())) {
            throw new IllegalStateException("La inscripción no pertenece al estudiante");
        }
        if (!reciboService.tieneAcudienteVinculado(insc.getEstudiante())) {
            throw new IllegalStateException("No hay acudiente registrado para este estudiante");
        }
        insc.setFechaSolicitudCertificadoAcudiente(LocalDateTime.now());
        inscripcionRepository.save(insc);

        List<Acudiente> vinculos = acudienteRepository
                .findByEstudianteDependienteIdEstudianteWithDetalle(insc.getEstudiante().getIdEstudiante());
        String nombreEst = insc.getEstudiante().getPersona().getNombre();
        String nombreCurso = insc.getCurso() != null ? insc.getCurso().getNombre() : "el curso";
        String base = publicUrl.replaceAll("/$", "");
        String loginUrl = base + "/login";
        String panelUrl = base + "/acudiente/panel";

        int enviados = 0;
        for (Acudiente v : vinculos) {
            Persona ap = v.getPersona();
            if (ap == null || ap.getEmail() == null || ap.getEmail().isBlank()) {
                continue;
            }
            String html = cuerpoHtml(nombreEst, nombreCurso, loginUrl, panelUrl);
            emailService.enviarHtml(ap.getEmail().trim(),
                    "Autorización de certificado — " + nombreEst + " (Sabor MasterClass)",
                    html);
            enviados++;
        }
        if (enviados == 0) {
            throw new IllegalStateException(
                    "Ningún acudiente tiene correo registrado. Actualiza el correo del acudiente en el perfil.");
        }
    }

    private String cuerpoHtml(String nombreEstudiante, String nombreCurso, String loginUrl, String panelUrl) {
        return """
                <div style="font-family:Segoe UI,sans-serif;max-width:560px;line-height:1.5;color:#222;">
                  <h2 style="color:#111;">Autorización de certificado</h2>
                  <p>El estudiante <strong>%s</strong> ha completado el curso <strong>%s</strong> y requiere que usted <strong>autorice la descarga del certificado</strong> desde el panel de acudiente.</p>
                  <ol>
                    <li><a href="%s">Iniciar sesión</a> con su cuenta de acudiente.</li>
                    <li>Abra <a href="%s">el panel del acudiente</a> y en «Cursos inscritos» use el botón para autorizar el certificado.</li>
                  </ol>
                  <p style="font-size:0.9em;color:#555;">Hasta que no se autorice, el estudiante no podrá descargar el PDF del certificado.</p>
                  <hr style="border:none;border-top:1px solid #eee;margin:1.5rem 0;">
                  <p style="font-size:0.85em;color:#888;">Sabor MasterClass</p>
                </div>
                """
                .formatted(nombreEstudiante, nombreCurso, loginUrl, panelUrl);
    }
}
