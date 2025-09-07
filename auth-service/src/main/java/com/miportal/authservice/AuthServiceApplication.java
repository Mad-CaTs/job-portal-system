package com.miportal.authservice;

import com.miportal.authservice.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableConfigurationProperties(JwtConfig.class)
@SpringBootApplication
public class AuthServiceApplication{
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}