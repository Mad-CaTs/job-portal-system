package com.miportal.authservice.model.dto.usuario;

import com.miportal.authservice.model.dto.rol.RolDTO;

import java.util.UUID;

public class UsuarioDTO {
    private UUID id;
    private String username;
    private String email;
    private String estado;
    private RolDTO rol;
}
