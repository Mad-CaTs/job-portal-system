package com.miportal.authservice.application.usecase;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.application.dto.auth.RefreshTokenRequest;
import com.miportal.authservice.application.dto.mapper.AuthMapper;
import com.miportal.authservice.application.exception.UnauthorizedException;
import com.miportal.authservice.application.in.AuthService;
import com.miportal.authservice.application.out.repository.RefreshTokenRepository;
import com.miportal.authservice.application.out.repository.UsuarioRepository;
import com.miportal.authservice.model.refreshToken.RefreshToken;
import com.miportal.authservice.model.usuario.Usuario;
import com.miportal.authservice.service.PasswordHasher;
import com.miportal.authservice.service.TokenProvider;
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
                3600000 // 1Hr
        );

        String refreshTokenStr = tokenProvider.generateRefreshToken(
                usuario.getEmail(),
                claims,
                604800000 // 7 días
        );

        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(refreshTokenStr)
                .expiracion(LocalDateTime.now().plusDays(7))
                .usuarioCreacion("system")
                .build();

        refreshTokenRepository.save(refreshToken);

        return authMapper.toLoginResponse(usuario, accessToken, refreshTokenStr);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token invalido"));

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

        );

        return authMapper.toAuthResponse(newAccessToken, request.getRefreshToken());
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }
}
