package com.miportal.authservice.application.in.service;

import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.application.dto.usuario.UsuarioUpdateRequest;

import java.util.List;

public interface UsuarioService {
    UsuarioDTO crearUsuario(UsuarioCreateRequest request);
    UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateRequest request);
    UsuarioDTO obtenerUsuarioPorId(Long id);
    List<UsuarioDTO> listarUsuarios();
    void eliminarUsuario(Long id);
}
