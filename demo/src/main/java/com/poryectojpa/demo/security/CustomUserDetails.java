package com.poryectojpa.demo.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.poryectojpa.demo.models.Persona;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Persona persona;

    public CustomUserDetails(Persona persona) {
        this.persona = persona;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Mapeo de roles según el valor de id_rol
        String roleName;
        roleName = switch (persona.getRolId()) {
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
        return persona.getNombre();
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
