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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
            String refreshToken = authService.getLastRefreshToken();

            // Crear cookie HttpOnly para refreshToken
            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(3600)
                    .sameSite("Lax")
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

    // ValidateToken - Validar Token JWT
    @Operation(
            summary = "Validar token JWT",
            description = "Valida si un token JWT es válido y no ha expirado. Usado por otros microservicios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token validado (ver response body para resultado)"),
            @ApiResponse(responseCode = "400", description = "Token no proporcionado")
    })
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(
            @Parameter(description = "Token JWT en header Authorization: Bearer <token>", required = true)
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Extraer token del header "Bearer <token>"
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Header Authorization inválido o faltante");
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "valid", false,
                                "error", "Header Authorization inválido"
                        ));
            }

            String token = authHeader.substring(7); // Remover "Bearer "

            boolean isValid = authService.validateToken(token);

            if (isValid) {
                // Si es válido, extraer claims adicionales
                Map<String, Object> claims = authService.extractTokenClaims(token);
                claims.put("valid", true);

                log.debug("Token válido para usuario: {}", claims.get("email"));
                return ResponseEntity.ok(claims);
            } else {
                log.debug("Token inválido");
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "error", "Token inválido o expirado"
                ));
            }
        } catch (Exception e) {
            log.error("Error al validar token: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "error", "Error interno al validar token"
            ));
        }
    }

    // getUserInfo - Obtener informacion del usuario
    @Operation(
            summary = "Obtener información de usuario desde token",
            description = "Extrae información completa del usuario desde un token JWT válido."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información extraída exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token inválido"),
            @ApiResponse(responseCode = "400", description = "Token no proporcionado")
    })
    @GetMapping("/user-info")
    public ResponseEntity<Map<String, Object>> getUserInfo(
            @Parameter(description = "Token JWT en header Authorization: Bearer <token>", required = true)
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Validar header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Header Authorization inválido"));
            }

            String token = authHeader.substring(7);

            Map<String, Object> userInfo = authService.getUserInfoFromToken(token);

            if (userInfo.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Token inválido o usuario no encontrado"));
            }

            log.debug("Información de usuario extraída para: {}", userInfo.get("email"));
            return ResponseEntity.ok(userInfo);

        } catch (Exception e) {
            log.error("Error al obtener información de usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    // validateRole - Validar si el usuario tiene un rol especifico
    @Operation(
            summary = "Validar rol de usuario",
            description = "Verifica si el usuario autenticado tiene un rol específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rol validado (ver response body)"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Token inválido")
    })
    @GetMapping("/validate-role/{role}")
    public ResponseEntity<Map<String, Object>> validateRole(
            @Parameter(description = "Rol a validar (POSTULANTE, EMPRESA, ADMIN)", required = true)
            @PathVariable String role,
            @Parameter(description = "Token JWT en header Authorization: Bearer <token>", required = true)
            @RequestHeader("Authorization") String authHeader) {

        try {
            // Validar header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "hasRole", false,
                                "error", "Header Authorization inválido"
                        ));
            }

            String token = authHeader.substring(7);

            // Validar rol
            boolean hasRole = authService.hasRole(token, role.toUpperCase());

            Map<String, Object> response = Map.of(
                    "hasRole", hasRole,
                    "requiredRole", role.toUpperCase()
            );

            log.debug("Validación de rol {} para usuario: {}", role, hasRole ? "PERMITIDO" : "DENEGADO");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error al validar rol: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                    "hasRole", false,
                    "error", "Error interno al validar rol"
            ));
        }
    }
}
