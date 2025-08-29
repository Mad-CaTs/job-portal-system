package com.miportal.authservice.application.service;

import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.application.dto.mapper.AuthMapper;
import com.miportal.authservice.application.exception.UnauthorizedException;
import com.miportal.authservice.application.port.out.RefreshTokenRepository;
import com.miportal.authservice.application.port.out.RolRepository;
import com.miportal.authservice.application.port.out.UsuarioRepository;
import com.miportal.authservice.application.usecase.AuthServiceImpl;
import com.miportal.authservice.config.JwtConfig;
import com.miportal.authservice.domain.model.rol.Rol;
import com.miportal.authservice.domain.model.usuario.Usuario;
import com.miportal.authservice.domain.service.PasswordHasher;
import com.miportal.authservice.domain.service.TokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private JwtConfig jwtConfig;

    @Test
    void loginDebeFallarSiEmailNoExiste(){
        // Arrange
        LoginRequest request = new LoginRequest("noexiste@test.com", "1234");
        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginDebeFallarSiPasswordIncorrecto(){
        // Arrange
        Usuario usuario = new Usuario().builder()
                .email("test@test.com")
                .password("hashed_password")
                .rol(Rol.builder().nombre("POSTULANTE").build())
                .build();

        LoginRequest request = new LoginRequest("test@test.com", "wrongpass");

        when(usuarioRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.of(usuario));
        when(passwordHasher.matches(request.getPassword(), usuario.getPassword()))
            .thenReturn(false);

        // Act + Assert
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void loginCorrectoDevolverTokens(){
        // Arrange
        Usuario usuario = Usuario.builder()
                .id(1L)
                .email("test@test.com")
                .password("hashed_password")
                .rol(Rol.builder().nombre("POSTULANTE").build())
                .build();

        LoginRequest request = new LoginRequest("test@test.com", "1234");

        when(usuarioRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.of(usuario));
        when(passwordHasher.matches(request.getPassword(), usuario.getPassword()))
            .thenReturn(true);

        when(jwtConfig.getAccessExpirationMillis()).thenReturn(900000L);
        when(jwtConfig.getRefreshExpirationMillis()).thenReturn(3600000L);

        when(tokenProvider.generateAccessToken(eq(usuario.getEmail()), anyMap(), anyLong()))
                .thenReturn("access_token");
        when(tokenProvider.generateRefreshToken(eq(usuario.getEmail()), anyMap(), anyLong()))
                .thenReturn("refresh_token");

        LoginResponse fakeResponse = LoginResponse.builder()
                .accessToken("access_token")
                .refreshToken("refresh_token")
                .username("test")
                .email("test@test.com")
                .rol("POSTULANTE")
                .build();

        when(authMapper.toLoginResponse(usuario, "access_token", "refresh_token"))
                .thenReturn(fakeResponse);

        // Act
        LoginResponse response = authService.login(request);
        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        assertEquals("test@test.com", response.getEmail());
        assertEquals("POSTULANTE", response.getRol());
    }
}
