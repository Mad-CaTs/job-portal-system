package com.miportal.authservice.adapter.out.persistence;

import com.miportal.authservice.application.port.out.RolRepository;
import com.miportal.authservice.domain.model.rol.Rol;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RolRepositoryAdapter implements RolRepository {

    private final SpringDataRolRepository repository;

    @Override
    public List<Rol> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Rol> findByNombre(String nombre) {
        return repository.findByNombre(nombre);
    }
}
