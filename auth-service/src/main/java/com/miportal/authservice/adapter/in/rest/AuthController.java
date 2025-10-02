package com.miportal.authservice.adapter.in.rest;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.application.dto.auth.RefreshTokenRequest;
import com.miportal.authservice.application.port.in.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "🔐 Autenticación", description = "Operaciones de autenticación y gestión de tokens JWT")
public class AuthController {

    private final AuthService authService;

    // Login - Iniciar Sesion
    @Operation(
            summary = "Iniciar Sesion",
            description = "Autentica usuario con email y contraseña. Retorna access token y establece refresh token en cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login Exitoso"),
            @ApiResponse(responseCode = "401", description = "Credenciales Invalidas"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Credenciales de Login", required = true)
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        try {
            LoginResponse loginResponse = authService.login(request);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                    .httpOnly(true)
                    .secure(false)                      // true en HTTPS (prod), false en HTTP (dev)
                    .path("/")
                    .maxAge(3600)
                    .sameSite("Lax")                    // Lax en dev, Strict en prod
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            log.error("Error en login para {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    // Logout - Cerrar Sesion
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el refresh token del usuario y elimina la cookie de sesión."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Logout exitoso"),
            @ApiResponse(responseCode = "400", description = "Refresh token no proporcionado")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Refresh token desde cookie", required = true)
            @CookieValue("refreshToken") String refreshToken,
            HttpServletResponse response) {

        try {
            authService.logout(refreshToken);

            // Eliminar cookie
            ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(0)
                    .sameSite("Lax")
                    .build();

            response.addHeader("Set-Cookie", cookie.toString());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error en logout: {}", e.getMessage());
            throw e;
        }
    }

    // RefreshToken - Renovar accessToken
    @Operation(
            summary = "Renovar access token",
            description = "Genera nuevo access token usando refresh token válido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente"),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado"),
            @ApiResponse(responseCode = "404", description = "Refresh token no encontrado")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Parameter(description = "Refresh token desde cookie", required = true)
            @CookieValue("refreshToken") String refreshToken) {

        try {
            RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al renovar token: {}", e.getMessage());
            throw e;
        }
    }
}
