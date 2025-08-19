package com.miportal.authservice.service;

import java.util.Map;

public interface TokenProvider {
    String generateAccessToken(String subject, Map<String, Object> claims, long expirationTimeMillis);
    String generateRefreshToken(String subject, Map<String, Object> claims, long expirationTimeMillis);
    boolean validateToken(String token);
    String getSubject(String token);
}
