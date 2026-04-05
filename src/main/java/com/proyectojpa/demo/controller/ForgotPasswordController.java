package com.proyectojpa.demo.controller;

import java.util.Locale;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.proyectojpa.demo.Service.EmailLayoutInstitucional;
import com.proyectojpa.demo.Service.EmailService;
import com.proyectojpa.demo.Service.PasswordResetTokenService;
import com.proyectojpa.demo.models.PasswordResetToken;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PersonaRepository;

@Controller
public class ForgotPasswordController {

    private static final Logger log = LoggerFactory.getLogger(ForgotPasswordController.class);

    private static final String MENSAJE_GENERICO =
            "Si existe una cuenta asociada a ese correo, recibirás un enlace para restablecer tu contraseña.";

    private final PersonaRepository personaRepository;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;
    private final String publicBaseUrl;
    private final String mailTransport;
    private final String mailUsername;
    private final String mailPassword;
    private final String resendApiKey;

    public ForgotPasswordController(
            PersonaRepository personaRepository,
            PasswordResetTokenService passwordResetTokenService,
            EmailService emailService,
            @Value("${app.public-url:http://localhost:8080}") String publicBaseUrl,
            @Value("${app.mail.transport:smtp}") String mailTransport,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword,
            @Value("${resend.api.key:}") String resendApiKey) {
        this.personaRepository = personaRepository;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
        this.publicBaseUrl = EmailLayoutInstitucional.baseUrlSinSlashFinal(publicBaseUrl);
        this.mailTransport = mailTransport != null ? mailTransport.trim().toLowerCase(Locale.ROOT) : "smtp";
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
        this.resendApiKey = resendApiKey;
    }

    @GetMapping("/forgot-password")
    public String mostrarFormulario() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String procesarSolicitud(@RequestParam(name = "email", required = false) String email, Model model) {
        if (email == null || email.isBlank()) {
            model.addAttribute("error", "Indica un correo electrónico.");
            return "forgot-password";
        }

        String normalized = email.trim().toLowerCase(Locale.ROOT);
        Optional<Persona> persona = personaRepository.findByEmailIgnoreCase(normalized);
        if (persona.isEmpty()) {
            Persona porEmailExacto = personaRepository.findByEmail(normalized);
            persona = porEmailExacto != null ? Optional.of(porEmailExacto) : Optional.empty();
        }

        if (persona.isEmpty()) {
            model.addAttribute("mensaje", MENSAJE_GENERICO);
            return "forgot-password";
        }

        Persona personaEncontrada = persona.get();

        if (!correoConfiguradoParaEnvio()) {
            if ("resend".equals(mailTransport)) {
                log.error(
                        "Resend sin clave: define RESEND_API_KEY y MAIL_FROM (remitente verificado en Resend).");
            } else {
                log.error(
                        "SMTP sin credenciales: define MAIL_USERNAME y MAIL_PASSWORD (Gmail: contraseña de aplicación, sin espacios).");
            }
            model.addAttribute("error",
                    "No pudimos enviar el correo en este momento. Intenta de nuevo más tarde.");
            return "forgot-password";
        }

        try {
            PasswordResetToken tokenEntity = passwordResetTokenService.crearToken(personaEncontrada);
            String link = publicBaseUrl + "/reset?token=" + tokenEntity.getToken();
            String html = construirHtmlRecuperacionContrasena(link);
            String destinatario = personaEncontrada.getEmail() != null ? personaEncontrada.getEmail() : normalized;
            emailService.enviarHtml(destinatario, "Recuperación de contraseña — Sabor MasterClass", html);
        } catch (Exception e) {
            log.error("Fallo al enviar recuperación de contraseña: {}", e.getMessage(), e);
            String hint = mensajeUsuarioSegunFalloCorreo(e, mailTransport);
            model.addAttribute("error", hint != null ? hint
                    : "No pudimos enviar el correo en este momento. Intenta de nuevo más tarde.");
            return "forgot-password";
        }

        model.addAttribute("mensaje", MENSAJE_GENERICO);
        return "forgot-password";
    }

    /**
     * Misma plantilla institucional que pago pendiente; el enlace de reset solo va en el botón (no URL a la vista).
     */
    private static String construirHtmlRecuperacionContrasena(String resetUrlAbsoluta) {
        String inner = """
                <p style="color: #555555; font-size: 16px; line-height: 1.6; margin-bottom: 25px;">
                    Hola,
                </p>
                <p style="color: #555555; font-size: 16px; line-height: 1.6; margin-bottom: 25px;">
                    Has solicitado <b>restablecer tu contraseña</b> en Sabor MasterClass.
                </p>
                <div style="background-color: #f9f9f9; padding: 25px; border-radius: 8px; border-left: 5px solid #6619f5; color: #444444; font-size: 16px; line-height: 1.6;">
                    <p style="margin: 0;">
                        Pulsa el botón de abajo para elegir una <b>nueva contraseña</b>. El enlace caduca pasado un tiempo por seguridad.
                    </p>
                </div>
                <p style="color: #555555; font-size: 15px; line-height: 1.6; margin-top: 22px; margin-bottom: 0;">
                    Si <b>no</b> solicitaste este cambio, ignora este mensaje; tu contraseña no se modificará.
                </p>
                """;
        return EmailLayoutInstitucional.pagina("&#128273; Recuperación de contraseña", inner,
                "ESTABLECER NUEVA CONTRASEÑA", resetUrlAbsoluta);
    }

    private boolean correoConfiguradoParaEnvio() {
        if ("resend".equals(mailTransport)) {
            return StringUtils.hasText(resendApiKey);
        }
        return StringUtils.hasText(mailUsername) && StringUtils.hasText(mailPassword);
    }

    private static final String AYUDA_RESEND_DOMINIO =
            "Resend rechazó el envío (validación). Aunque MAIL_FROM sea correcto (p. ej. noreply@tudominio.com), "
                    + "en resend.com el dominio debe figurar como verificado (DNS SPF/DKIM/MX según indiquen), sin errores. "
                    + "Comprueba que MAIL_FROM coincida exactamente con una dirección de ese dominio y que la propagación DNS haya terminado. "
                    + "Si usas cuenta de prueba, revisa las restricciones de destinatarios en la documentación de Resend.";

    private static final String AYUDA_SMTP_TIMEOUT_RENDER =
            "No se pudo abrir conexión SMTP (tiempo de espera agotado). En Render (y otros PaaS) suele estar bloqueada "
                    + "la salida hacia smtp.gmail.com en los puertos 587 y 465: no suele ser un fallo de la contraseña "
                    + "de aplicación. Opciones estables: (1) MAIL_TRANSPORT=resend, RESEND_API_KEY y dominio verificado "
                    + "en Resend; (2) alojar la app en un VPS que permita SMTP. Como prueba opcional en Render: "
                    + "MAIL_PORT=465, MAIL_SMTP_SSL=true, MAIL_STARTTLS_ENABLE=false.";

    /**
     * Mensaje útil sin filtrar secretos (patrones conocidos de Resend/SMTP).
     */
    private static String mensajeUsuarioSegunFalloCorreo(Exception e, String mailTransport) {
        String transport = mailTransport != null ? mailTransport.trim().toLowerCase(Locale.ROOT) : "smtp";
        for (Throwable t = e; t != null; t = t.getCause()) {
            String msg = t.getMessage();
            if (msg == null) {
                continue;
            }
            String m = msg.toLowerCase(Locale.ROOT);
            boolean resend422 = m.contains("resend http 422");
            boolean resendCuerpo = m.contains("resend http");
            if (resend422 || m.contains("validation_error") || m.contains("domain is not verified")
                    || m.contains("invalid from") || (resendCuerpo && m.contains("not verified"))) {
                return AYUDA_RESEND_DOMINIO;
            }
            if (m.contains("resend http 403") || m.contains("invalid api key") || m.contains("resend http 401")) {
                return "La clave de envío (Resend) no es válida o no tiene permiso. Revisa RESEND_API_KEY en Render y que sea la del mismo proyecto/región que el dominio.";
            }
            if (m.contains("connect timed out") || m.contains("mailconnectexception")
                    || m.contains("connection timed out") || m.contains("sockettimeoutexception")) {
                if ("resend".equals(transport)) {
                    return "No se pudo conectar a la API de Resend (red o cortafuegos). Revisa conectividad del servidor y que RESEND_API_KEY sea correcta.";
                }
                return AYUDA_SMTP_TIMEOUT_RENDER;
            }
        }
        return null;
    }

}
