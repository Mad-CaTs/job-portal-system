package com.miportal.authservice.application.dto.mapper;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.model.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    @Mapping(source = "usuario.username", target = "username")
    @Mapping(source = "usuario.email", target = "email")
    @Mapping(source = "usuario.rol.nombre", target = "rol")
    LoginResponse toLoginResponse(Usuario usuario, String token, String refreshToken);

    @Mapping(target = "authenticated", constant = "true")
    @Mapping(target = "message", constant = "Token renovado")
    @Mapping(source = "accessToken", target = "accessToken")
    @Mapping(source = "refreshToken", target = "refreshToken")
    AuthResponse toAuthResponse(String accessToken, String refreshToken);
}
