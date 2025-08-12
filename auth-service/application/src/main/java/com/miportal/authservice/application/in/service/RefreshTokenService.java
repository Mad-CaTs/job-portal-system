package com.miportal.authservice.application.in.service;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.RefreshTokenRequest;

public interface RefreshTokenService {
    String crearRefreshToken(Long usuarioId);
    AuthResponse refrescarToken(RefreshTokenRequest request);
    void eliminarRefreshToken(String refreshToken);
}
