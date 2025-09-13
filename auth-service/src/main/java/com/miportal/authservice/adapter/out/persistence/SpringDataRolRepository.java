package com.miportal.authservice.adapter.out.persistence;

import com.miportal.authservice.domain.model.rol.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataRolRepository extends JpaRepository<Rol, Long>{
    Optional<Rol> findByNombre(String nombre);
}
