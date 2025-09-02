package com.miportal.authservice.domain.service;

import io.jsonwebtoken.Claims;

import java.util.Map;

public interface TokenProvider {
    String generateAccessToken(String subject, Map<String, Object> claims, long expirationTimeMillis);
    String generateRefreshToken(String subject, Map<String, Object> claims, long expirationTimeMillis);
    boolean validateToken(String token);
    String getSubject(String token);
    Claims getAllClaims(String token);
}
