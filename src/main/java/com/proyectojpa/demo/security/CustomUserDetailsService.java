package com.proyectojpa.demo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proyectojpa.demo.domain.InscripcionEstados;
import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.EstudianteRepository;
import com.proyectojpa.demo.repository.PersonaRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private PersonaRepository PersonaRepository;

    @Autowired
    private EstudianteRepository estudianteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.debug("Carga de usuario por email");
        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Email vacío");
        }
        String trimmed = email.trim();
        Persona persona = PersonaRepository.findByEmailIgnoreCase(trimmed)
                .orElseGet(() -> PersonaRepository.findByEmail(trimmed));
        if (persona == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + email);
        }

        boolean cuentaHabilitada = true;
        if (persona.getRolId() != null && persona.getRolId() == 2) {
            cuentaHabilitada = estudianteRepository.findByPersonaWithEstado(persona)
                    .map(e -> (Boolean) (e.getEstadoEstudiante() != null
                            && e.getEstadoEstudiante().getCodigo() != null
                            && InscripcionEstados.ACTIVO.equals(e.getEstadoEstudiante().getCodigo())))
                    .orElse(false);
        }

        return new CustomUserDetails(persona, cuentaHabilitada);
    }
}
