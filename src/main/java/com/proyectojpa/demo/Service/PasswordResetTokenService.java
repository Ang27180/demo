package com.proyectojpa.demo.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.proyectojpa.demo.models.PasswordResetToken;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PasswordResetTokenRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Service
public class PasswordResetTokenService {

    private static final Duration VIGENCIA_TOKEN = Duration.ofHours(1);
    private static final int LONGITUD_MINIMA_CONTRASENA = 6;

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PersonaRepository personaRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public PasswordResetTokenService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            PersonaRepository personaRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.personaRepository = personaRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    /**
     * Crea y persiste un token de recuperación para la persona indicada.
     */
    public PasswordResetToken crearToken(Persona persona) {
        if (persona == null) {
            throw new IllegalArgumentException("La persona no puede ser nula");
        }

        PasswordResetToken entity = new PasswordResetToken();
        entity.setToken(UUID.randomUUID().toString());
        entity.setFechaExpiracion(LocalDateTime.now().plus(VIGENCIA_TOKEN));
        entity.setPersona(persona);

        return passwordResetTokenRepository.save(entity);
    }

    /**
     * Valida el token (existe y no ha expirado) y devuelve la entidad asociada.
     *
     * @throws IllegalArgumentException si el token es nulo/vacío, no existe o ya expiró
     */
    public PasswordResetToken validarToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("El token de recuperación es obligatorio");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new IllegalArgumentException("El token de recuperación no es válido"));

        if (resetToken.getFechaExpiracion() == null
                || !resetToken.getFechaExpiracion().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("El token de recuperación ha expirado");
        }

        return resetToken;
    }

    /**
     * Valida el token y la nueva contraseña, persiste el hash BCrypt en {@link Persona} y borra todos los
     * {@link PasswordResetToken} de esa persona.
     *
     * @throws IllegalArgumentException token inválido o expirado, contraseña demasiado corta o datos de recuperación inconsistentes
     */
    @Transactional
    public void completarCambioContrasena(String token, String nuevaContraseña) {
        if (!StringUtils.hasText(nuevaContraseña) || nuevaContraseña.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }

        PasswordResetToken resetToken = validarToken(token);
        Persona ref = resetToken.getPersona();
        if (ref == null || ref.getId() == null) {
            throw new IllegalArgumentException("Datos de recuperación no válidos");
        }

        Persona persona = personaRepository.findById(ref.getId())
                .orElseThrow(() -> new IllegalArgumentException("Datos de recuperación no válidos"));

        persona.setContrasena(bCryptPasswordEncoder.encode(nuevaContraseña));
        personaRepository.save(persona);
        passwordResetTokenRepository.deleteByPersona_Id(persona.getId());
    }
}
