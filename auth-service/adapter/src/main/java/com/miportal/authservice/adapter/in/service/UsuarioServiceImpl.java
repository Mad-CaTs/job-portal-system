package com.miportal.authservice.adapter.in.service;

import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.application.dto.usuario.UsuarioUpdateRequest;
import com.miportal.authservice.application.exception.NotFoundException;
import com.miportal.authservice.application.in.service.UsuarioService;
import com.miportal.authservice.application.mapper.UsuarioMapper;
import com.miportal.authservice.application.out.repository.RolRepository;
import com.miportal.authservice.application.out.repository.UsuarioRepository;
import com.miportal.authservice.application.exception.ConflictException;
import com.miportal.authservice.model.rol.Rol;
import com.miportal.authservice.model.usuario.Usuario;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final UsuarioMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UsuarioDTO crearUsuario(UsuarioCreateRequest request) {
        //Verificar si el username ya existe
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("El nombre de usuario ya está en uso");
        }
        //Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El correo electrónico ya está en uso");
        }
        //Validar si el rol existe
        Rol rolEntity = rolRepository.findByNombre(request.getRol())
                .orElseThrow(() -> new NotFoundException("Rol no encontrado: " + request.getRol()));

        Usuario usuario = mapper.toEntity(request);
        //Encriptar password
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        //Asignar rol al usuario en memoria
        usuario.setRoles(Set.of(rolEntity));
        //Guardar usuario en la entidad
        Usuario guardado = usuarioRepository.save(usuario);
        //Convierte la entidad a DTO y la devuelve
        return mapper.toDTO(guardado);
    }

    @Override
    @Transactional
    public UsuarioDTO actualizarUsuario(Long id, UsuarioUpdateRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id" + id));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if(usuarioRepository.existsByEmail(request.getEmail()) &&
                    !usuario.getEmail().equals(request.getEmail())) {
                throw new ConflictException("El correo electronico ya esta en uso");
            }
            usuario.setEmail(request.getEmail());
        }

        if(request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.getPassword())); //Encriptar Password
        }

        if(request.getEstado() != null) {
            usuario.setEstado(request.getEstado());
        }

        if(request.getRol() != null && !request.getRol().isBlank()) {
            Rol rolEntity = rolRepository.findByNombre(request.getRol())
                    .orElseThrow(() -> new NotFoundException("Rol no encontrado" +  request.getRol()));
            usuario.setRoles(Set.of(rolEntity));
        }

        Usuario actualizado =  usuarioRepository.save(usuario);
        return mapper.toDTO(actualizado);
    }

    @Override
    public UsuarioDTO obtenerUsuarioPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con id" + id));
        return mapper.toDTO(usuario);
    }

    @Override
    public List<UsuarioDTO> listarUsuarios() {
        return mapper.toDTOList(usuarioRepository.findAll());
    }

    @Override
    @Transactional
    public void eliminarUsuario(Long id) {
        if(!usuarioRepository.existById(id)){
            throw new NotFoundException("Usuario no encontrado con id" + id);
        }
        usuarioRepository.deleteById(id);
    }
}
