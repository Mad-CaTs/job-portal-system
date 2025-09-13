package com.miportal.authservice.adapter.out.persistence;

import com.miportal.authservice.application.port.out.RefreshTokenRepository;
import com.miportal.authservice.domain.model.refreshToken.RefreshToken;
import com.miportal.authservice.domain.model.usuario.Usuario;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final SpringDataRefreshTokenRepository repository;

    @Override
    public RefreshToken save(RefreshToken token) {
        return repository.save(token);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return repository.findByToken(token);
    }

    @Override
    public void delete(RefreshToken token) {
        repository.delete(token);
    }

    @Override
    @Transactional
    public void deleteByUsuario(Usuario usuario) {
        repository.deleteByUsuario(usuario);
    }
}
