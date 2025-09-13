package com.miportal.authservice.adapter.out.security;

import com.miportal.authservice.config.JwtConfig;
import com.miportal.authservice.domain.service.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProviderImpl implements TokenProvider {

    private final JwtConfig jwtConfig;

    @Override
    public String generateAccessToken(String subject, Map<String, Object> claims, long expirationTimeMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeMillis))
                .signWith(jwtConfig.getSigningKey())
                .compact();
    }

    @Override
    public String generateRefreshToken(String subject, Map<String, Object> claims, long expirationTimeMillis) {
        return generateAccessToken(subject, claims, expirationTimeMillis);
    }

    @Override
    public boolean validateToken(String token) {
        try{
            Jwts.parserBuilder().setSigningKey(jwtConfig.getSigningKey()).build().parseClaimsJws(token);
            return true;
        }catch(JwtException e){
            return false;
        }
    }

    @Override
    public String getSubject(String token) {
        return Jwts.parserBuilder().setSigningKey(jwtConfig.getSigningKey()).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    @Override
    public Claims getAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtConfig.getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody(); // Devuelve todos los claims del JWT
    }
}
