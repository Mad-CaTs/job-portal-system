package com.miportal.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {
    // Aquí se guarda como usuario auditor
    @Bean
    public AuditorAware<String> auditorProvider(){
        return () -> Optional.ofNullable(
              Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                      .filter(Authentication::isAuthenticated)
                      .map(Authentication::getName)
                      .orElse("system") // Fallback
        );
    }
}
