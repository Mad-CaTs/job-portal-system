package com.miportal.authservice.application.dto.mapper;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.domain.model.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    @Mapping(target = "username", source = "usuario.username")
    @Mapping(target = "email", source = "usuario.email")
    @Mapping(target = "rol", expression = "java(usuario.getRol().getNombre())")
    @Mapping(target = "accessToken", expression = "java(accessToken)")
    @Mapping(target = "refreshToken", expression = "java(refreshToken)")
    LoginResponse toLoginResponse(Usuario usuario, String accessToken, String refreshToken);

    @Mapping(target = "authenticated", expression = "java(true)")
    @Mapping(target = "message", constant = "Autenticación exitosa")
    @Mapping(target = "accessToken", expression = "java(accessToken)")
    @Mapping(target = "refreshToken", expression = "java(refreshToken)")
    AuthResponse toAuthResponse(String accessToken, String refreshToken);
}
