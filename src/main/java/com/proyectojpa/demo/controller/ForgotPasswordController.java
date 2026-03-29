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
    private final String mailUsername;
    private final String mailPassword;

    public ForgotPasswordController(
            PersonaRepository personaRepository,
            PasswordResetTokenService passwordResetTokenService,
            EmailService emailService,
            @Value("${app.public-url:http://localhost:8080}") String publicBaseUrl,
            @Value("${spring.mail.username:}") String mailUsername,
            @Value("${spring.mail.password:}") String mailPassword) {
        this.personaRepository = personaRepository;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
        this.publicBaseUrl = trimTrailingSlash(publicBaseUrl);
        this.mailUsername = mailUsername;
        this.mailPassword = mailPassword;
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

        if (!StringUtils.hasText(mailUsername) || !StringUtils.hasText(mailPassword)) {
            log.error(
                    "SMTP sin credenciales: define MAIL_USERNAME y MAIL_PASSWORD (contraseña de aplicación Gmail, sin espacios) y reinicia la aplicación.");
            model.addAttribute("error",
                    "No pudimos enviar el correo en este momento. Intenta de nuevo más tarde.");
            return "forgot-password";
        }

        try {
            PasswordResetToken tokenEntity = passwordResetTokenService.crearToken(personaEncontrada);
            String link = publicBaseUrl + "/reset?token=" + tokenEntity.getToken();
            String cuerpo = construirCuerpoCorreo(link);
            String destinatario = personaEncontrada.getEmail() != null ? personaEncontrada.getEmail() : normalized;
            emailService.enviarTexto(destinatario, "Recuperación de contraseña — Sabor MasterClass", cuerpo);
        } catch (Exception e) {
            log.warn("Fallo SMTP al enviar recuperación de contraseña (revisa credenciales Gmail y red/puerto 587)", e);
            model.addAttribute("error",
                    "No pudimos enviar el correo en este momento. Intenta de nuevo más tarde.");
            return "forgot-password";
        }

        model.addAttribute("mensaje", MENSAJE_GENERICO);
        return "forgot-password";
    }

    private static String construirCuerpoCorreo(String link) {
        return """
                Hola,

                Has solicitado restablecer tu contraseña en Sabor MasterClass.

                Abre el siguiente enlace (válido por tiempo limitado):
                %s

                Si no solicitaste este cambio, ignora este mensaje.
                """.formatted(link);
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return "http://localhost:8080";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
