package com.miportal.authservice.adapter.in.rest;

import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.application.port.in.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UsuarioService usuarioService;

    // ENDPOINTS DE REGISTRO
    @PostMapping("/registro/postulante")
    public ResponseEntity<UsuarioDTO> registrarPostulante(@Valid @RequestBody
            UsuarioCreateRequest request) {

        request.setRol("POSTULANTE");
        UsuarioDTO usuario = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(usuario);
    }

    @PostMapping("/registro/empresa")
    public ResponseEntity<UsuarioDTO> registrarEmpresa(
            @Valid @RequestBody UsuarioCreateRequest request) {

        request.setRol("EMPRESA");
        UsuarioDTO usuario = usuarioService.crearUsuario(request);
        return ResponseEntity.ok(usuario);
    }
}
