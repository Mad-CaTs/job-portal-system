package com.miportal.authservice.application.dto.usuario;

import com.miportal.authservice.application.dto.rol.RolDTO;
import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String username;
    private String email;
    private Boolean estado;
    private RolDTO rol;
}
