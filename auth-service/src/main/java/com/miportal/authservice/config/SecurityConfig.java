package com.miportal.authservice.config;

import com.miportal.authservice.adapter.out.security.CustomUserDetailsService;
import com.miportal.authservice.adapter.out.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

// * RESPONSABILIDADES:
// * - Configurar autenticación JWT
// * - Definir rutas públicas (SOLO auth endpoints)
// * - Configurar CORS para comunicación entre servicios
// * - Configurar filtros de seguridad

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity // Permite @PreAuthorize encontroladores y servicios
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // Endpoints de autenticación
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/auth/refresh",

                                // Endpoints para validación desde otros microservicios
                                "/api/auth/validate",
                                "/api/auth/user-info",
                                "/api/auth/validate-role/**",

                                // Documentación API (Swagger)
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Logout requiere estar autenticado (para obtener refresh token)
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                // Agregar filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Configuracion de Spring Security para autenticar usuarios
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // BCrypt para hash de contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

//     * CONFIGURACIÓN DE CORS
//     * Permite requests desde:
//     * - Frontend (Angular)
//     * - Otros microservicios
//     * - API Gateway
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        // Orígenes permitidos
        cfg.setAllowedOrigins(List.of(
                "http://localhost:4200",         // Angular
                "http://localhost:8080",         // Auth-service
                "http://localhost:8081",         // Postulante-service
                "http://localhost:8082",         // Empresa-service
                "http://localhost:8083",         // Admin-service
                "http://localhost:8090"          // API Gateway
        ));

        // Métodos HTTP permitidos
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));

        // Headers permitidos
        cfg.setAllowedHeaders(List.of(
                "Authorization",                 // Para JWT tokens
                "Content-Type",                  // Para JSON requests
                "X-Requested-With",              // Para AJAX requests
                "Accept",                        // Para especificar formato de respuesta
                "Origin",                        // Para CORS
                "Access-Control-Request-Method", // Para preflight requests
                "Access-Control-Request-Headers" // Para preflight requests
        ));

        // Headers expuestos en la respuesta
        cfg.setExposedHeaders(List.of(
                "Authorization",                 // Para que el frontend pueda leer tokens
                "Content-Disposition"            // Para downloads de archivos
        ));

        // Permitir envío de cookies (para refresh token)
        cfg.setAllowCredentials(true);

        // Aplicar configuración a todas las rutas
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}

