package com.miportal.authservice.application.usecase;

import com.miportal.authservice.application.dto.mapper.UsuarioMapper;
import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.application.dto.usuario.UsuarioUpdateRequest;
import com.miportal.authservice.application.exception.ConflictException;
import com.miportal.authservice.application.exception.NotFoundException;
import com.miportal.authservice.application.port.in.UsuarioService;
import com.miportal.authservice.application.port.out.RolRepository;
import com.miportal.authservice.application.port.out.UsuarioRepository;
import com.miportal.authservice.domain.model.rol.Rol;
import com.miportal.authservice.domain.model.usuario.Usuario;
import com.miportal.authservice.domain.service.PasswordHasher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordHasher passwordHasher;
    private final UsuarioMapper usuarioMapper;

    @Override
    public UsuarioDTO crearUsuario(UsuarioCreateRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email ya está registrado");
        }
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("El nombre de usuario ya está registrado");
        }

        Rol rol = rolRepository.findByNombre(request.getRol())
                .orElseThrow(() -> new NotFoundException("Rol no encontrado"));

        Usuario usuario = usuarioMapper.toEntity(request);
        usuario.setPassword(passwordHasher.hash(request.getPassword()));
        usuario.setRol(rol);

        return usuarioMapper.toDTO(usuarioRepository.save(usuario));
    }

    @Override
    public UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Usuario no encontrado"));

        if(request.getEmail() !=null && !request.getEmail().equals(usuario.getEmail())){
            if(usuarioRepository.existsByEmail(request.getEmail())){
                throw new ConflictException("El email ya esta registrado");
            }
            usuario.setEmail(request.getEmail());
        }

        if(request.getPassword() !=null){
            usuario.setPassword(passwordHasher.hash(request.getPassword()));
        }

        if (request.getEstado() != null) {
            usuario.setEstado(request.getEstado());
        }

        if (request.getRol() != null) {
            Rol rol = rolRepository.findByNombre(request.getRol())
                    .orElseThrow(()->new NotFoundException("Rol no encontrado"));
            usuario.setRol(rol);
        }

        usuario.setUsuarioModificacion("system");

        Usuario updated = usuarioRepository.save(usuario);
        return usuarioMapper.toDTO(updated);
    }

    @Override
    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(()->new NotFoundException("Usuario no encontrado"));
        return usuarioMapper.toDTO(usuario);
    }

    @Override
    public List<UsuarioDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(usuarioMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new NotFoundException("Usuario no encontrado");
        }
        usuarioRepository.deleteById(id);
    }
}
