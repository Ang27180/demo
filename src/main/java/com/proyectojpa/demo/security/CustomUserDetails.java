package com.proyectojpa.demo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.proyectojpa.demo.models.Persona;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Persona persona;
    /** Para ESTUDIANTE: solo true si {@code estado_inscripcion.codigo == ACTIVO}. Otros roles: true. */
    private final boolean accountEnabled;

    public CustomUserDetails(Persona persona, boolean accountEnabled) {
        this.persona = persona;
        this.accountEnabled = accountEnabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapeo de roles según id_rol (null-safe: evita NullPointerException en el switch)
        Integer rid = persona.getRolId();
        if (rid == null) {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }
        String roleName = switch (rid) {
            case 1 -> "ROLE_ADMIN";
            case 2 -> "ROLE_ESTUDIANTE";
            case 3 -> "ROLE_TUTOR";
            case 4 -> "ROLE_PROVEEDOR";
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
        return persona.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() {
        return accountEnabled;
    }

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
