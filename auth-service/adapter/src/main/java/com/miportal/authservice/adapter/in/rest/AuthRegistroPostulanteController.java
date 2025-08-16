package com.miportal.authservice.adapter.in.rest;

import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.application.in.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/registro")
@RequiredArgsConstructor
public class AuthRegistroPostulanteController {

    private final UsuarioService usuarioService;

    @PostMapping("/postulante")
    public ResponseEntity<UsuarioDTO> registrarPostulante(@Valid @RequestBody UsuarioCreateRequest request){
        request.setRol("POSTULANTE");
        return ResponseEntity.ok(usuarioService.crearUsuario(request));
    }
}
