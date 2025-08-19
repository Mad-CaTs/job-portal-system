package com.miportal.authservice.application.dto.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private boolean authenticated;
    private String message;
    private String accessToken;
    private String refreshToken;
}
