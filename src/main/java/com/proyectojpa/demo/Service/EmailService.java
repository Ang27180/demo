package com.proyectojpa.demo.Service;

import jakarta.mail.MessagingException;

/**
 * Envío de correo vía SMTP (p. ej. Gmail con {@code JavaMailSender}).
 * <p>
 * Ejemplo para recuperación de contraseña (texto plano con enlace):
 * </p>
 * <pre>{@code
 * String baseUrl = "http://localhost:8080"; // en producción usa la propiedad app.public-url
 * String token = "..."; // UUID u otro token seguro
 * String link = baseUrl + "/reset?token=" + token;
 * String cuerpo = "Para restablecer tu contraseña, abre este enlace:\n\n" + link;
 * emailService.enviarTexto(destinatarioEmail, "Recuperación de contraseña", cuerpo);
 * }</pre>
 */
public interface EmailService {

    /**
     * Correo de texto plano.
     *
     * @param destinatario dirección de correo del destinatario
     * @param asunto       asunto del mensaje
     * @param mensaje      cuerpo en texto plano
     */
    void enviarTexto(String destinatario, String asunto, String mensaje);

    void enviarHtml(String para, String asunto, String html) throws MessagingException;

    void enviarConAdjunto(String para, String asunto, String mensaje, String rutaArchivo) throws MessagingException;
}
