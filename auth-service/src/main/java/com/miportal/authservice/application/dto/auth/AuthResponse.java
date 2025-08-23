package com.miportal.authservice.application.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private boolean authenticated;
    private String message;
    private String accessToken;
    private String refreshToken;
}
