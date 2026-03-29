package com.proyectojpa.demo.Service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

/**
 * Prueba real de SMTP: requiere MySQL accesible (mismo perfil que el arranque normal) y credenciales de correo.
 * <p>
 * Ejecutar: {@code mvn test -Dtest=EmailServiceEnvioTextoIT}
 * <p>
 * Variables de entorno necesarias para envío Gmail: {@code MAIL_USERNAME}, {@code MAIL_PASSWORD}
 * (contraseña de aplicación, sin espacios si Gmail la muestra en bloques).
 */
@SpringBootTest
@ActiveProfiles("test")
class EmailServiceEnvioTextoIT {

    @Autowired
    private EmailService emailService;

    @Test
    @DisplayName("EmailService.enviarTexto — recuperación de contraseña (SMTP real)")
    void enviarTexto_recuperacionPrueba() {
        // Solo variables de entorno (no los placeholders de application-test.properties)
        String user = System.getenv("MAIL_USERNAME");
        String pass = System.getenv("MAIL_PASSWORD");
        assumeTrue(StringUtils.hasText(user) && StringUtils.hasText(pass),
                "Omitido: exporta MAIL_USERNAME y MAIL_PASSWORD (contraseña de aplicación Gmail) para probar envío real.");

        assertDoesNotThrow(() -> emailService.enviarTexto(
                "test@example.com",
                "Recuperación de contraseña",
                "Link de prueba: http://localhost:8080/reset?token=abc123"));
    }
}
