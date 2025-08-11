package com.miportal.authservice.adapter.in.rest;

import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.application.in.service.UsuarioService;
import com.miportal.authservice.model.usuario.Usuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<UsuarioDTO> crearUsuario(@RequestBody @Valid UsuarioCreateRequest request) {
        UsuarioDTO usuario = usuarioService.crearUsuario(request);
        URI location = URI.create("/api/usuarios/" + usuario.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(usuario);
    }

}
