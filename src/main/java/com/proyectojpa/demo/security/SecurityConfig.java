package com.proyectojpa.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

                        .requestMatchers(HttpMethod.GET, "/reportes/certificado/pdf/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/reportes/recibo/pdf/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/reportes/estadistico/pdf").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/reportes", "/reportes/").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/inscripciones/catalogo").permitAll()
                        .requestMatchers("/inscripciones/**").hasRole("ESTUDIANTE")

                        .requestMatchers(HttpMethod.GET, "/tutor").permitAll()
                        .requestMatchers(HttpMethod.POST, "/tutor").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tutor/**").hasRole("ADMIN")
                        .requestMatchers("/tutor/panel/**").hasRole("TUTOR")

                        .requestMatchers("/contacto", "/registro", "/login", "/forgot-password", "/css/**", "/js/**",
                                "/imagenes/**", "/files/medios-pago/**", "/favicon.ico", "/error").permitAll()

                        .requestMatchers("/admin/**", "/personas/**", "/personas/exportarExcel", "/correo/formulario")
                                .hasRole("ADMIN")

                        .requestMatchers("/estudiante/**", "/mis-cursos/**", "/acudiente")
                                .hasAnyRole("ADMIN", "ESTUDIANTE")

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
                                redirectUrl = "/tutor/panel/cursos";
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
