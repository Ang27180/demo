package com.proyectojpa.demo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.proyectojpa.demo.models.Persona;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Persona persona;

    public CustomUserDetails(Persona persona, boolean cuentaHabilitada) {
        this.persona = persona;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // CORRECCIÓN: Protección contra rolId null para evitar NullPointerException
        if (persona.getRolId() == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        // Mapeo de id_rol → ROLE_XXX que Spring Security entiende
        String roleName = switch (persona.getRolId()) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_ESTUDIANTE";
            case 3 -> "ROLE_TUTOR";
            case 4 -> "ROLE_ACUDIENTE"; // AJUSTE: Vinculado a la funcionalidad de Acudiente
            default -> "ROLE_USER";
        };
        return List.of(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return persona.getContrasena();
    }

    @Override
    public String getUsername() {
        // CORRECCIÓN: Spring Security usa getUsername() para el lookup.
        // Debe retornar el email (que es el campo con el que se autentica),
        // NO el nombre de la persona.
        return persona.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public Persona getPersona() {
        return persona;
    }

    public String getNombre() {
        return persona.getNombre();
    }

    // (opcional) si quieres usarlo también:
    public String getnombre() {
        return persona.getNombre();
    }
}
