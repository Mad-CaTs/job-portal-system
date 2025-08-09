package com.miportal.authservice.application.dto.usuario;

import com.miportal.authservice.application.dto.rol.RolDTO;

public class UsuarioDTO {
    private Long id;
    private String username;
    private String email;
    private String estado;
    private RolDTO rol;
}
