package com.miportal.authservice.application.service;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.exception.UnauthorizedException;
import com.miportal.authservice.application.port.out.RefreshTokenRepository;
import com.miportal.authservice.application.port.out.RolRepository;
import com.miportal.authservice.application.port.out.UsuarioRepository;
import com.miportal.authservice.application.usecase.AuthServiceImpl;
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

    @Test
    void loginDebeFallarSiUsuarioNoExiste(){
        // Arrange
        LoginRequest request = new LoginRequest("noexiste@test.com", "1234");
        when(usuarioRepository.findByEmail(request.getEmail()))
                .thenReturn(Optional.empty());
    }

    @Test
    void loginDebeFallarSiPasswordIncorrecto(){
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("test@test.com");
        usuario.setPassword("hashed_password");

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
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@test.com");
        usuario.setPassword("hashed_password");

        LoginRequest request = new LoginRequest("test@test.com", "1234");

        when(usuarioRepository.findByEmail(request.getEmail()))
            .thenReturn(Optional.of(usuario));
        when(passwordHasher.matches(request.getPassword(), usuario.getPassword()))
            .thenReturn(true);
        when(tokenProvider.generateAccessToken(usuario))
                .thenReturn("access_token");
        when(tokenProvider.generateRefreshToken(usuario))
                .thenReturn("refresh_token");

        // Act
        AuthResponse response = authService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
    }
}
