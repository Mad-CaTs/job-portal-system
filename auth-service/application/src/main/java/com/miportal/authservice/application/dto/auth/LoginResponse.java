package com.miportal.authservice.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String username;
    private String email;
    private String rol;
}
