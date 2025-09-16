package com.miportal.authservice.application.usecase;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.application.dto.auth.RefreshTokenRequest;
import com.miportal.authservice.application.dto.mapper.AuthMapper;
import com.miportal.authservice.application.exception.NotFoundException;
import com.miportal.authservice.application.exception.UnauthorizedException;
import com.miportal.authservice.application.port.in.AuthService;
import com.miportal.authservice.application.port.out.RefreshTokenRepository;
import com.miportal.authservice.application.port.out.UsuarioRepository;
import com.miportal.authservice.config.JwtConfig;
import com.miportal.authservice.domain.model.refreshToken.RefreshToken;
import com.miportal.authservice.domain.model.usuario.Usuario;
import com.miportal.authservice.domain.service.PasswordHasher;
import com.miportal.authservice.domain.service.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// * RESPONSABILIDADES ÚNICAS:
// * - Login/Logout de usuarios
// * - Generación y validación de tokens JWT
// * - Gestión de refresh tokens
// * - Validación de tokens para otros microservicios

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordHasher passwordHasher;
    private final TokenProvider tokenProvider;
    private final AuthMapper authMapper;
    private final JwtConfig jwtConfig;

    // Variable para almacenar temporalmente el refreshToken
    private final ThreadLocal<String> currentRefreshToken = new ThreadLocal<>();

    /**
     * Metodo auxiliar para obtener el refreshToken del último login
     */
    @Override
    public String getLastRefreshToken() {
        String token = currentRefreshToken.get();
        currentRefreshToken.remove(); // Limpiar después de usar
        return token;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Credenciales invalidas"));
        if(!passwordHasher.matches(request.getPassword(), usuario.getPassword())){
            throw new UnauthorizedException("Credenciales invalidas");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().getNombre());
        claims.put("username", usuario.getUsername());
        claims.put("userId", usuario.getId());

        String accessToken = tokenProvider.generateAccessToken(
                usuario.getEmail(),
                claims,
                jwtConfig.getAccessExpirationMillis()
        );

        String refreshTokenStr = tokenProvider.generateRefreshToken(
                usuario.getEmail(),
                claims,
                jwtConfig.getRefreshExpirationMillis()
        );

        currentRefreshToken.set(refreshTokenStr);

        // Eliminar refresh tokens anteriores del usuario
        refreshTokenRepository.deleteByUsuario(usuario);

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(refreshTokenStr)
                .expiracion(LocalDateTime.now().plusHours(1))
                .usuarioCreacion("system")
                .build();

        refreshTokenRepository.save(refreshToken);

        log.info("Login exitoso para usuario: {} con rol: {}", usuario.getEmail(), usuario.getRol().getNombre());
        return authMapper.toLoginResponse(usuario, accessToken);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> {
                    return new NotFoundException("Refresh token no encontrado");
                });

        if(token.getExpiracion().isBefore(LocalDateTime.now())) {
            log.warn("Refresh token expirado, eliminando de BD");
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token expirado");
        }

        Usuario usuario = token.getUsuario();

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().getNombre());
        claims.put("username", usuario.getUsername());
        claims.put("userId", usuario.getId());

        String newAccessToken =  tokenProvider.generateAccessToken(
                usuario.getEmail(),
                claims,
                jwtConfig.getAccessExpirationMillis()
        );

        log.info("Token renovado exitosamente para usuario: {}", usuario.getEmail());
        return authMapper.toAuthResponse(newAccessToken, request.getRefreshToken());
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            boolean isValid = tokenProvider.validateToken(token);
            log.debug("Validación de token: {}", isValid ? "VÁLIDO" : "INVÁLIDO");
            return isValid;
        } catch (Exception e) {
            log.warn("Error al validar token: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Object> extractTokenClaims(String token) {
        try {
            if(!validateToken(token)) {
                log.warn("Intento de extraer claims de token inválido");
                return new HashMap<>();
            }

            String email = tokenProvider.getSubject(token);
            var claims = tokenProvider.getAllClaims(token);

            Map<String, Object> tokenInfo = new HashMap<>();
            tokenInfo.put("email", email);
            tokenInfo.put("rol", claims.get("rol"));
            tokenInfo.put("username", claims.get("username"));
            tokenInfo.put("userId", claims.get("userId"));
            tokenInfo.put("exp", claims.getExpiration());
            tokenInfo.put("iat", claims.getIssuedAt());

            log.debug("📊 Claims extraídos exitosamente para usuario: {}", email);
            return tokenInfo;
        } catch (Exception e) {
            log.error("Error al extraer claims del token: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public Map<String, Object> getUserInfoFromToken(String token) {
        try {
            if(!validateToken(token)) {
                return new HashMap<>();
            }

            String email = tokenProvider.getSubject(token);
            Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

            if(usuario == null) {
                log.warn("Usuario no encontrado para token válido: {}", email);
                return new HashMap<>();
            }

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", usuario.getId());
            userInfo.put("email", usuario.getEmail());
            userInfo.put("username", usuario.getUsername());
            userInfo.put("rol", usuario.getRol().getNombre());
            userInfo.put("estado", usuario.getEstado());
            return userInfo;
        } catch (Exception e) {
            log.error("Error al obtener información de usuario: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public boolean hasRole(String token, String requiredRole) {
        try {
            Map<String, Object> claims = extractTokenClaims(token);
            String userRole = (String) claims.get("rol");

            boolean hasPermission = requiredRole.equals(userRole);
            log.debug("Validación de rol {} para usuario: {}", requiredRole, hasPermission ? "PERMITIDO" : "DENEGADO");
            return hasPermission;
        } catch (Exception e) {
            log.error("Error al validar rol: {}", e.getMessage());
            return false;
        }
    }
}
