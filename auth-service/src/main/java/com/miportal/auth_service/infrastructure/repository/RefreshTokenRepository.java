package com.miportal.auth_service.infrastructure.repository;

import com.miportal.auth_service.domain.model.RefreshToken;
import com.miportal.auth_service.domain.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUsuario(Usuario usuario);
}