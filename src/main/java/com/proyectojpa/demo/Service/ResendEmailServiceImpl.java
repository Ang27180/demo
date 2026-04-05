package com.proyectojpa.demo.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Envío vía API HTTPS de <a href="https://resend.com">Resend</a> (puerto 443). Adecuado cuando el hosting bloquea SMTP (p. ej. Render).
 */
@Service
@ConditionalOnProperty(name = "app.mail.transport", havingValue = "resend")
public class ResendEmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailServiceImpl.class);

    private static final String RESEND_BASE = "https://api.resend.com";

    /**
     * Resend solo permite remitentes de dominios que hayas verificado en su panel.
     * Incluye @resend.dev (p. ej. onboarding@resend.dev): solo pruebas; en producción debe usarse el dominio verificado.
     */
    private static final Set<String> DOMINIOS_NO_PERMITIDOS_COMO_FROM = Set.of(
            "resend.dev",
            "gmail.com",
            "googlemail.com",
            "yahoo.com",
            "yahoo.es",
            "hotmail.com",
            "outlook.com",
            "live.com",
            "msn.com",
            "icloud.com",
            "me.com",
            "mac.com");

    private final RestClient client;
    private final String from;
    private final ObjectMapper objectMapper;
    private final String replyTo;

    public ResendEmailServiceImpl(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${resend.api.key:}") String apiKey,
            @Value("${app.mail.from:${spring.mail.username}}") String from,
            @Value("${app.mail.from-verified-fallback:noreply@send.sabormasterclass.com}") String fromVerifiedFallback,
            @Value("${app.mail.reply-to:}") String replyTo) {
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException(
                    "app.mail.transport=resend requiere la variable de entorno RESEND_API_KEY (clave API de Resend).");
        }
        if (!StringUtils.hasText(from)) {
            throw new IllegalStateException(
                    "Define app.mail.from o MAIL_FROM con una dirección verificada en Resend (o spring.mail.username).");
        }
        String trimmed = from.trim();
        if (usaDominioCorreoPublicoComoFrom(trimmed)) {
            String fb = fromVerifiedFallback != null ? fromVerifiedFallback.trim() : "";
            if (!StringUtils.hasText(fb) || usaDominioCorreoPublicoComoFrom(fb)) {
                throw new IllegalStateException(
                        "app.mail.from / MAIL_FROM apunta a un dominio público (p. ej. Gmail) y "
                                + "app.mail.from-verified-fallback / MAIL_FROM_VERIFIED no es un remitente válido para Resend. "
                                + "Define MAIL_FROM o MAIL_FROM_VERIFIED con una dirección @tu-dominio-verificado.");
            }
            log.info(
                    "Remitente '{}' no es válido para Resend; usando dominio verificado: {}",
                    trimmed,
                    fb);
            trimmed = fb;
        }
        this.from = trimmed;
        this.objectMapper = objectMapper;
        this.replyTo = StringUtils.hasText(replyTo) ? replyTo.trim() : null;
        this.client = restClientBuilder
                .baseUrl(RESEND_BASE)
                .defaultHeader("Authorization", "Bearer " + apiKey.trim())
                .build();
    }

    static boolean usaDominioCorreoPublicoComoFrom(String fromHeader) {
        String email = extraerEmail(fromHeader);
        if (email.isEmpty()) {
            return true;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return true;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return DOMINIOS_NO_PERMITIDOS_COMO_FROM.contains(domain);
    }

    private static String extraerEmail(String fromHeader) {
        if (fromHeader == null) {
            return "";
        }
        int lt = fromHeader.indexOf('<');
        int gt = fromHeader.indexOf('>');
        if (lt >= 0 && gt > lt) {
            return fromHeader.substring(lt + 1, gt).trim().toLowerCase(Locale.ROOT);
        }
        return fromHeader.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public void enviarTexto(String destinatario, String asunto, String mensaje) {
        Map<String, Object> body = baseBody(destinatario, asunto);
        body.put("text", mensaje);
        postEmails(body);
    }

    @Override
    public void enviarHtml(String para, String asunto, String html) throws MessagingException {
        Map<String, Object> body = baseBody(para, asunto);
        body.put("html", html);
        try {
            postEmails(body);
        } catch (MailSendException e) {
            throw new MessagingException("Resend: " + e.getMessage(), e);
        }
    }

    @Override
    public void enviarConAdjunto(String para, String asunto, String mensaje, String rutaArchivo)
            throws MessagingException {
        Map<String, Object> body = baseBody(para, asunto);
        body.put("text", mensaje);
        File file = new File(rutaArchivo);
        try {
            String b64 = java.util.Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            Map<String, String> att = new LinkedHashMap<>();
            att.put("filename", file.getName().isEmpty() ? "archivo" : file.getName());
            att.put("content", b64);
            body.put("attachments", List.of(att));
            postEmails(body);
        } catch (IOException e) {
            throw new MessagingException("No se pudo leer el adjunto: " + rutaArchivo, e);
        } catch (MailSendException e) {
            throw new MessagingException("Resend: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> baseBody(String destinatario, String asunto) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("from", from);
        body.put("to", List.of(destinatario.trim()));
        body.put("subject", asunto);
        if (replyTo != null) {
            body.put("reply_to", replyTo);
        }
        return body;
    }

    private void postEmails(Map<String, Object> body) {
        final String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new MailSendException("Resend: no se pudo serializar el cuerpo JSON", e);
        }
        try {
            client.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            String detail = e.getResponseBodyAsString(StandardCharsets.UTF_8);
            log.warn("Resend rechazó el envío: HTTP {} — {}", e.getStatusCode().value(), detail);
            throw new MailSendException("Resend HTTP " + e.getStatusCode().value() + ": " + detail, e);
        } catch (Exception e) {
            throw new MailSendException("Resend: " + e.getMessage(), e);
        }
    }
}
