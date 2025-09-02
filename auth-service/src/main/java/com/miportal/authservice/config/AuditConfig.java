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
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null ||  !authentication.isAuthenticated()) {
                return Optional.of("system"); // Si no hay auth usamos system
            }

            String username = authentication.getName();

            if("anonymousUser".equals(username)) {
                return Optional.of("system"); // Reemplazamos anonymousUser
            }
            return Optional.of(username);
        };
    }
}
