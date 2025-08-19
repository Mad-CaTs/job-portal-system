package com.miportal.authservice.config;

import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.security.Key;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {

    private String secret;
    private long accessExpirationMillis;
    private long refreshExpirationMillis;

    public Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
