package com.proyectojpa.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Define el {@link PasswordEncoder} del contexto Spring (BCrypt).
 * <p>
 * Uso típico al persistir una {@code Persona} (el campo en entidad es {@code contrasena}):
 * </p>
 * <pre>{@code
 * // Inyectar en servicio o componente:
 * private final PasswordEncoder passwordEncoder;
 *
 * // Antes de personaRepository.save(persona):
 * String plano = persona.getContrasena(); // texto del formulario
 * persona.setContrasena(passwordEncoder.encode(plano));
 * personaRepository.save(persona);
 * }</pre>
 * <p>
 * Para comprobar una contraseña en login u otros flujos: {@code passwordEncoder.matches(plano, hashEnBd)}.
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
