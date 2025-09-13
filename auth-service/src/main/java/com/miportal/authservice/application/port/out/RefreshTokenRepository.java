package com.miportal.authservice.application.port.out;

import com.miportal.authservice.domain.model.refreshToken.RefreshToken;
import com.miportal.authservice.domain.model.usuario.Usuario;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String token);
    void delete(RefreshToken token);
    void deleteByUsuario(Usuario usuario);
}
