package com.poryectojpa.demo.security;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.poryectojpa.demo.models.Persona;
import com.poryectojpa.demo.repository.personaRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private personaRepository personaRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        System.out.println("Intentando autenticar: " + email);
        Persona persona = personaRepository.findByEmail(email);
        System.out.println("Resultado: " + persona);
        if (persona == null) {
            throw new UsernameNotFoundException("Usuario no encontrado con el email: " + email);
        }
        return new CustomUserDetails(persona);        
    }
}
