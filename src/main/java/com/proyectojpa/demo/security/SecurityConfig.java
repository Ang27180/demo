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

                        //  PERMITIR TODO LO DE CORREO
                        .requestMatchers("/correo/**").permitAll()
                        .requestMatchers("/correo/formulario", "/correo/enviar-desde-vista").permitAll()

                        // Certificados y recibos PDF: autenticados (autorización fina en controlador/servicio)
                        .requestMatchers(HttpMethod.GET, "/reportes/certificado/pdf/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/reportes/recibo/pdf/**").authenticated()

                        .requestMatchers("/reportes/**").permitAll()

                        // API Tutor: listado público; mutaciones solo administrador
                        .requestMatchers(HttpMethod.GET, "/tutor").permitAll()
                        .requestMatchers(HttpMethod.POST, "/tutor").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tutor/**").hasRole("ADMIN")

                        //  PÚBLICOS
                        .requestMatchers("/home", "/nosotros", "/contacto", "/registro", "/login",
                                "/forgot-password", "/css/**", "/imagenes/**", "/files/medios-pago/**").permitAll()

                        // ADMIN
                        .requestMatchers("/admin/**", "/personas/**", "/personas/exportarExcel", "/correo/formulario")
                                .hasRole("ADMIN")

                        //  ADMIN + ESTUDIANTE
                        .requestMatchers("/cursos", "/cursos/**", "/estudiante/**", "/mis-cursos/**")
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
