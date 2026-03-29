package com.proyectojpa.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.GET, "/", "/home", "/nosotros", "/nuestros-tutores").permitAll()
                        .requestMatchers(HttpMethod.GET, "/cursos", "/cursos/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/cursos/*/lecciones/*/completar").hasRole("ESTUDIANTE")

                        .requestMatchers("/correo/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/reportes/certificado/pdf/**").hasAnyRole("ESTUDIANTE", "ACUDIENTE")
                        .requestMatchers(HttpMethod.GET, "/reportes/recibo/pdf/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/reportes/estadistico/pdf").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reportes", "/reportes/").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/inscripciones/catalogo").permitAll()
                        .requestMatchers("/inscripciones/**").hasRole("ESTUDIANTE")

                        .requestMatchers(HttpMethod.GET, "/tutor").permitAll()
                        .requestMatchers(HttpMethod.POST, "/tutor").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tutor/**").hasRole("ADMIN")
                        .requestMatchers("/tutor-panel", "/tutor-panel-estudiantes", "/tutor/panel/**")
                                .hasAnyRole("ADMIN", "TUTOR")

                        // Gestión de contenido del curso: mismo flujo que admin; el tutor solo si el curso es suyo (validado en controlador)
                        .requestMatchers(HttpMethod.GET, "/admin/cursos/*/contenido").hasAnyRole("ADMIN", "TUTOR")
                        .requestMatchers(HttpMethod.POST, "/admin/cursos/*/modulos").hasAnyRole("ADMIN", "TUTOR")
                        .requestMatchers(HttpMethod.POST, "/admin/cursos/modulos/*/lecciones").hasAnyRole("ADMIN", "TUTOR")
                        .requestMatchers(HttpMethod.GET, "/admin/cursos/modulos/eliminar/*").hasAnyRole("ADMIN", "TUTOR")
                        .requestMatchers(HttpMethod.GET, "/admin/cursos/lecciones/eliminar/*").hasAnyRole("ADMIN", "TUTOR")

                        .requestMatchers("/contacto", "/registro", "/login", "/forgot-password", "/reset", "/css/**", "/js/**",
                                "/imagenes/**", "/files/medios-pago/**", "/favicon.ico", "/error").permitAll()

                        .requestMatchers("/admin/**", "/personas/**", "/personas/exportarExcel", "/correo/formulario")
                                .hasRole("ADMIN")

                        .requestMatchers("/estudiante/**", "/mis-cursos/**")
                                .hasAnyRole("ADMIN", "ESTUDIANTE")

                        // AJUSTE: Permisos para la nueva vista de Acudiente
                        .requestMatchers("/acudiente/**").hasAnyRole("ADMIN", "ACUDIENTE")

                        .anyRequest().authenticated())

                // LOGIN
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            String curso = request.getParameter("curso");
                            if (curso != null && !curso.isBlank()) {
                                try {
                                    int idCurso = Integer.parseInt(curso.trim());
                                    if (authentication.getAuthorities().stream()
                                            .anyMatch(a -> "ROLE_ESTUDIANTE".equals(a.getAuthority()))) {
                                        response.sendRedirect(request.getContextPath()
                                                + "/inscripciones/nueva?idCurso=" + idCurso);
                                        return;
                                    }
                                } catch (NumberFormatException ignored) {
                                    // sigue flujo por rol
                                }
                            }
                            var authorities = authentication.getAuthorities();
                            String redirectUrl = "/home";
                            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                                redirectUrl = "/admin";
                            } else if (authorities.stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ESTUDIANTE"))) {
                                redirectUrl = "/estudiante";
                            } else if (authorities.stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_TUTOR"))) {
                                redirectUrl = "/tutor-panel";
                            } else if (authorities.stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ACUDIENTE"))) {
                                // AJUSTE: Redirección especial para el rol 4
                                redirectUrl = "/acudiente/panel";
                            }
                            response.sendRedirect(request.getContextPath() + redirectUrl);
                        })
                        .failureUrl("/login?error=true")
                        .permitAll())

                // LOGOUT
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/home?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userDetailsService;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Permitir todos los orígenes
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false); // No se permiten credenciales si se usa "*"
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
