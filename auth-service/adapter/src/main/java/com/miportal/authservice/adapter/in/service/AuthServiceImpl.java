package com.miportal.authservice.adapter.in.service;

import com.miportal.authservice.application.dto.auth.AuthResponse;
import com.miportal.authservice.application.dto.auth.LoginRequest;
import com.miportal.authservice.application.dto.auth.LoginResponse;
import com.miportal.authservice.application.dto.auth.RefreshTokenRequest;
import com.miportal.authservice.application.exception.NotFoundException;
import com.miportal.authservice.application.exception.UnauthorizedException;
import com.miportal.authservice.application.in.service.AuthService;
import com.miportal.authservice.application.mapper.AuthMapper;
import com.miportal.authservice.application.out.repository.UsuarioRepository;
import com.miportal.authservice.model.usuario.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final AuthMapper authMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        //Buscar usuario por email
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new NotFoundException("Usuario no encontrado"));
        //Verificar si las contraseñas concuerdan
        if(!passwordEncoder.matches(request.getPassword(), usuario.getPassword())){
            throw new UnauthorizedException("Credenciales inválidas");
        }

        String accessToken = jwtTokenProvider.generateToken(usuario);
        String refreshToken = refreshTokenService.createRefreshToken(usuario.getId());

        return authMapper.toLoginResponse(usuario, accessToken, refreshToken);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.refresh(request.getRefreshToken());
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.delete(refreshToken);
    }
}
