package com.miportal.authservice.application.port.in;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.application.dto.auth.RefreshTokenRequest;

import java.util.Map;

public interface AuthService {
    String getLastRefreshToken();
    LoginResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String refreshToken);
    boolean validateToken(String token);
    Map<String, Object> extractTokenClaims(String token);
    Map<String, Object> getUserInfoFromToken(String token);
    boolean hasRole(String token, String requiredRole);
}
