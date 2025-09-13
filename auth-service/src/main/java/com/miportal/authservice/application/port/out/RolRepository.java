package com.miportal.authservice.application.port.out;

import com.miportal.authservice.domain.model.rol.Rol;

import java.util.List;
import java.util.Optional;

public interface RolRepository {
    List<Rol> findAll();
    Optional<Rol> findByNombre(String nombre);
}
