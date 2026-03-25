package com.poryectojpa.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // CORRECCIÓN: Un solo @Autowired del servicio de usuarios personalizado
    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // ================================================================
    // Bean principal: Proveedor de autenticación con BCrypt + email
    // CORRECCIÓN: Se declara explícitamente y se conecta al HttpSecurity
    // ================================================================
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        // Usa nuestro servicio que busca por email
        provider.setUserDetailsService(customUserDetailsService);
        // Usa BCrypt para comparar contraseñas (igual que en el registro)
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // ================================================================
    // PasswordEncoder: BCrypt (mismo que se usa al registrar usuarios)
    // ================================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ================================================================
    // AuthenticationManager: necesario para que Spring Security pueda
    // procesar el login correctamente
    // ================================================================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ================================================================
    // Cadena de filtros de seguridad
    // CORRECCIÓN: Se registra explícitamente el authenticationProvider
    // ================================================================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Registrar explícitamente el proveedor de autenticación
        http.authenticationProvider(authenticationProvider());

        http
            .authorizeHttpRequests(auth -> auth

                // Recursos estáticos y correo
                .requestMatchers("/css/**", "/imagenes/**", "/js/**", "/webjars/**").permitAll()
                .requestMatchers("/correo/**").permitAll()
                .requestMatchers("/reportes/**").permitAll()

                // Endpoint tutor público
                .requestMatchers("/tutor").permitAll()

                // Páginas públicas (sin login)
                .requestMatchers(
                    "/", "/home", "/nosotros", "/contacto",
                    "/registro", "/login", "/acudiente",
                    "/forgot-password", "/cursos", "/ver-curso/**"
                ).permitAll()

                // Solo ADMIN
                .requestMatchers(
                    "/admin", "/admin/**",
                    "/personas/**",
                    "/personas/exportarExcel",
                    "/cursos/admin/**"
                ).hasRole("ADMIN")

                // ADMIN, ESTUDIANTE o TUTOR (Páginas de contenido y perfil)
                .requestMatchers("/estudiante/**", "/mis-cursos/**", "/perfil/**", "/tutor-panel/**", "/tutor-panel-estudiantes/**", "/cursos/**")
                    .hasAnyRole("ADMIN", "ESTUDIANTE", "TUTOR")

                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            )

            // Configuración del formulario de login
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                // Redirige según el rol después de autenticar exitosamente
                .successHandler((request, response, authentication) -> {
                    var roles = authentication.getAuthorities();
                    String destino = "/home"; // default

                    if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
                        destino = "/admin";
                    } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_ESTUDIANTE"))) {
                        destino = "/estudiante";
                    } else if (roles.stream().anyMatch(r -> r.getAuthority().equals("ROLE_TUTOR"))) {
                        destino = "/tutor-panel"; // Redirigir al Panel de Tutor
                    }

                    response.sendRedirect(destino);
                })
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // Configuración del logout
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )

            // CSRF deshabilitado (proyecto académico / Postman)
            .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
