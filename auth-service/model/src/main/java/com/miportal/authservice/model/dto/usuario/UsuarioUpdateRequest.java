package com.miportal.authservice.model.dto.usuario;

public class UsuarioUpdateRequest {
    private String email;
    private String password;
    private String estado; // ACTIVO / INACTIVO
    private String rol;
}
