package com.miportal.authservice.application.port.out;

import com.miportal.authservice.domain.model.usuario.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Usuario save(Usuario usuario);
    Optional<Usuario> findById(Long id);
    Optional<Usuario> findByEmail(String email);
    List<Usuario> findAll();
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsById(Long id);
    void deleteById(Long id);
}
