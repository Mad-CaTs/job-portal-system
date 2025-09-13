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
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

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

        return authMapper.toLoginResponse(usuario, accessToken);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new NotFoundException("Refresh token no encontrado"));

        if(token.getExpiracion().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token expirado");
        }

        Usuario usuario = token.getUsuario();

        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().getNombre());

        String newAccessToken =  tokenProvider.generateAccessToken(
                usuario.getEmail(),
                claims,
                jwtConfig.getAccessExpirationMillis()
        );

        return authMapper.toAuthResponse(newAccessToken, request.getRefreshToken());
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }
}
