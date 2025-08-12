package com.miportal.authservice.adapter.in.service;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.RefreshTokenRequest;
import com.miportal.authservice.application.exception.NotFoundException;
import com.miportal.authservice.application.exception.UnauthorizedException;
import com.miportal.authservice.application.in.service.RefreshTokenService;
import com.miportal.authservice.application.mapper.AuthMapper;
import com.miportal.authservice.application.out.repository.RefreshTokenRepository;
import com.miportal.authservice.application.out.repository.UsuarioRepository;
import com.miportal.authservice.model.refreshToken.RefreshToken;
import com.miportal.authservice.model.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuthMapper authMapper;
    @Override
    public String crearRefreshToken(Long usuarioId) {
        // Verificar si el usuario existe
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        RefreshToken token = new RefreshToken();
        token.setToken(UUID.randomUUID().toString());
        token.setUsuario(usuario);
        token.setExpiracion(LocalDateTime.now().plusDays(7)); //Expira en 7 dias

        refreshTokenRepository.save(token);

        return token.getToken();
    }

    @Override
    public AuthResponse refrescarToken(RefreshTokenRequest request) {
        // Verificar si el refreshToken Existe
        RefreshToken token = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(()-> new NotFoundException("Refresh token no encontrado"));

        // Verificar expiracion del token
        if(token.getExpiracion().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token expirado. Por favor inicia sesion nuevamente");
        }

        Usuario usuario = token.getUsuario();
        String newAccessToken = jwtTokenProvider.generarToken(usuario);

        return authMapper.toLoginResponse(usuario, newAccessToken, token.getToken());
    }

    @Override
    public void eliminarRefreshToken(String refreshToken) {

    }
}
