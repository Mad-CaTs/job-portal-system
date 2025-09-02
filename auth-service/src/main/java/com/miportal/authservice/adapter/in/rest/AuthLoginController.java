package com.miportal.authservice.adapter.in.rest;

import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.application.port.in.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthLoginController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        LoginResponse loginResponse = authService.login(request);

        // Crear cookie segura para refreshToken
        ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                .httpOnly(true)       // No accesible desde JS
                .secure(true)         // Solo HTTPS
                .path("/api/auth/refresh") // Solo se envía a este endpoint
                .sameSite("Strict")   // Previene CSRF
                .maxAge(60 * 60)      // 1 hora (igual a refreshToken en DB)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // Quitamos el refreshToken del body para más seguridad
        loginResponse.setRefreshToken(null);

        return ResponseEntity.ok(loginResponse);
    }
}
