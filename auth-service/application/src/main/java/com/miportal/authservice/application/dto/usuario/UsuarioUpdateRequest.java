package com.miportal.authservice.application.dto.usuario;

public class UsuarioUpdateRequest {
    private String email;
    private String password;
    private String estado; // ACTIVO / INACTIVO
    private String rol;
}
