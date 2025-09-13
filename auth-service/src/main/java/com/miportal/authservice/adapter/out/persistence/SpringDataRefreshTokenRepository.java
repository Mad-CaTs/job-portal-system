package com.miportal.authservice.adapter.out.persistence;

import com.miportal.authservice.domain.model.refreshToken.RefreshToken;
import com.miportal.authservice.domain.model.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
}
