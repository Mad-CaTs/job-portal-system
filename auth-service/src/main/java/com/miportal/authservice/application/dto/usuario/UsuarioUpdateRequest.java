package com.miportal.authservice.application.dto.usuario;

import lombok.Data;

@Data
public class UsuarioUpdateRequest {
    private String email;
    private String password;
    private Boolean estado;
    private String rol;
}
