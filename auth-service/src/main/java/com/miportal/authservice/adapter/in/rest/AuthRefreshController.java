//package com.miportal.authservice.adapter.in.rest;
//
//import com.miportal.authservice.application.dto.auth.AuthResponse;
//import com.miportal.authservice.application.dto.auth.RefreshTokenRequest;
//import com.miportal.authservice.application.port.in.AuthService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthRefreshController {
//    private final AuthService authService;
//
//    @PostMapping("/refresh")
//    public ResponseEntity<AuthResponse> refreshToken(@CookieValue("refreshToken") String refreshToken) {
//        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
//        return ResponseEntity.ok(authService.refreshToken(request));
//    }
//}
