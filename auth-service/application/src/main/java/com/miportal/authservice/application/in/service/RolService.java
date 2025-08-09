package com.miportal.authservice.application.in.service;

import com.miportal.authservice.application.dto.rol.RolDTO;

import java.util.List;

public interface RolService {
    RolDTO crearRol(RolDTO request);
    RolDTO actualizarRol(Long id, RolDTO request);
    RolDTO obtenerRolPorId(Long id);
    List<RolDTO> listarRoles();
    void eliminarRol(Long id);
}
