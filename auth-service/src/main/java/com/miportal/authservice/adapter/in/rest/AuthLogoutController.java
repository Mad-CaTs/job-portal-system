//package com.miportal.authservice.adapter.in.rest;
//
//import com.miportal.authservice.application.port.in.AuthService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseCookie;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthLogoutController {
//
//    private final AuthService authService;
//
//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(@CookieValue("refreshToken") String refreshToken,
//                                       HttpServletResponse response) {
//        authService.logout(refreshToken);
//
//        // Eliminar Cookie
//        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
//                .httpOnly(true)
//                .secure(false)        // false en desarrollo
//                .path("/")            // path raíz, no específico
//                .maxAge(0)
//                .sameSite("Lax")      // Añadir para compatibilidad
//                .build();
//
//        response.addHeader("Set-Cookie", cookie.toString());
//
//        return ResponseEntity.noContent().build();
//    }
//}

