package com.miportal.authservice.adapter.out.security;

import com.miportal.authservice.domain.service.TokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    // Rutas públicas que no requieren token
    private static final List<String> EXCLUDED_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/registro/**",
            "/api/auth/refresh",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html",
            "/swagger-resources",
            "/webjars"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Excluir rutas públicas (match con patrones)
        if (isExcluded(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            if (tokenProvider.validateToken(jwt)) {
                String subject = tokenProvider.getSubject(jwt); // Email

                // Intentamos extraer la claim "rol" desde el token
                Optional<String> maybeRole = extractRoleFromToken(jwt);

                List<GrantedAuthority> authorities;
                if (maybeRole.isPresent()) {
                    // Creamos la autoridad directamente desde la claim
                    String rol = maybeRole.get();
                    authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                } else {
                    // Fallback: cargamos userDetails desde DB (ya devuelve authorities correctas)
                    UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
                    authorities = List.copyOf(userDetails.getAuthorities());
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(subject, null, authorities);

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isExcluded(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * Intenta obtener la claim "rol" desde el token usando tokenProvider.getAllClaims(token).
     * Si tu TokenProvider no implementa getAllClaims(...) el método lanzará UnsupportedOperationException
     * y el filtro hará fallback a cargar UserDetails desde DB.
     */
    private Optional<String> extractRoleFromToken(String token) {
        try {
            Claims claims = tokenProvider.getAllClaims(token);
            if (claims != null) {
                String rol = claims.get("rol", String.class);
                if (rol != null && !rol.isBlank()) {
                    return Optional.of(rol);
                }
            }
        } catch (UnsupportedOperationException | NoSuchMethodError ex) {
            // El TokenProvider no provee getAllClaims -> fallback (silencioso)
        } catch (Exception ex) {
            // Cualquier otro error en parsing devolvemos empty para fallback seguro
        }
        return Optional.empty();
    }
}
