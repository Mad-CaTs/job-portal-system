package com.miportal.authservice.application.mapper;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.model.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AuthMapper {
    // Convertir a LoginResponseDTO
    LoginResponse toLoginResponse(Usuario usuario, String accessToken, String refreshToken);

    // Envolvemos un LoginResponse dentro de AuthResponse
    default AuthResponse toAuthResponse(Usuario usuario, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .authenticated(true)
                .message("Token refrescado correctamente")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
