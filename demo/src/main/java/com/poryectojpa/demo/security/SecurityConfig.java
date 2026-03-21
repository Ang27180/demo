package com.poryectojpa.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth

                        //  PERMITIR TODO LO DE CORREO
                        .requestMatchers("/correo/**").permitAll()
                        .requestMatchers("/correo/formulario", "/correo/enviar-desde-vista").permitAll()
                        .requestMatchers("/reportes/**").permitAll()
                        
                        //  ENDPOINT TUTOR 100% PÚBLICO (GET y POST)
                        .requestMatchers("/tutor").permitAll()

                        //  PÚBLICOS
                        .requestMatchers("/home", "/nosotros", "/contacto", "/registro", "/login", "/acudiente",
                                "/forgot-password", "/css/**", "/imagenes/**").permitAll()

                        // ADMIN
                        .requestMatchers("/admin/**", "/personas/**", "/personas/exportarExcel","/correo/formulario")
                                .hasRole("ADMIN")

                        //  ADMIN + ESTUDIANTE
                        .requestMatchers("/cursos", "/estudiante/**")
                                .hasAnyRole("ADMIN", "ESTUDIANTE")

                        // Todo lo demás requiere login
                        .anyRequest().authenticated())

                // LOGIN
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            var authorities = authentication.getAuthorities();
                            String redirectUrl = "/home"; // por defecto
                            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                                redirectUrl = "/admin";
                            } else if (authorities.stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ESTUDIANTE"))) {
                                redirectUrl = "/estudiante";
                            }
                            response.sendRedirect(redirectUrl);
                        })
                        .failureUrl("/login?error=true")
                        .permitAll())

                // LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                // Desactivar CSRF para Postman
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }
}
