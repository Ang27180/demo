package com.proyectojpa.demo.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proyectojpa.demo.models.Persona;
import com.proyectojpa.demo.repository.PersonaRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PersonaRepository PersonaRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        System.out.println("Intentando autenticar: " + email);
        Persona persona = PersonaRepository.findByEmail(email);
        System.out.println("Resultado: " + persona);
        if (persona == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + email);
        }
        return new CustomUserDetails(persona);        
    }
}
