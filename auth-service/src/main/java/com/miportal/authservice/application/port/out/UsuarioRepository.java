package com.miportal.authservice.application.port.out;

import com.miportal.authservice.domain.model.usuario.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<Usuario> findByEmail(String email);
}
