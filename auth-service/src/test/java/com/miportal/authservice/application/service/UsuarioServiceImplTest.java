package com.miportal.authservice.application.service;

import com.miportal.authservice.application.dto.mapper.UsuarioMapper;
import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.application.dto.usuario.UsuarioUpdateRequest;
import com.miportal.authservice.application.exception.ConflictException;
import com.miportal.authservice.application.exception.NotFoundException;
import com.miportal.authservice.application.port.out.RolRepository;
import com.miportal.authservice.application.port.out.UsuarioRepository;
import com.miportal.authservice.application.usecase.UsuarioServiceImpl;
import com.miportal.authservice.domain.model.rol.Rol;
import com.miportal.authservice.domain.model.usuario.Usuario;
import com.miportal.authservice.domain.service.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private UsuarioMapper usuarioMapper;

    @InjectMocks
    private UsuarioServiceImpl usuarioServiceImpl;

    @Test
    void crearUsuarioDebeFallarSiEmailYaExiste() {
        UsuarioCreateRequest request = UsuarioCreateRequest.builder()
                .email("test@test.com")
                .password("1234")
                .username("user")
                .rol("POSTULANTE")
                .build();

        when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(true);
        assertThrows(ConflictException.class, () -> usuarioServiceImpl.crearUsuario(request));
    }

    @Test
    void crearUsuarioDebeGuardarYRetornarDTO(){
        UsuarioCreateRequest request = UsuarioCreateRequest.builder()
                .email("nuevo@test.com")
                .password("1234")
                .username("nuevo")
                .rol("POSTULANTE")
                .build();

        // Entidad simulada que devolvera el mapper -> entidad antes de persistir
        Usuario usuarioEntity = Usuario.builder()
                .id(1L)
                .username("nuevo")
                .email("nuevo@test.com")
                .password("hashed_password")
                .rol(Rol.builder().id(1L).nombre("POSTULANTE").build())
                .build();

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(1L)
                .username("nuevo")
                .email("nuevo@test.com")
                .rol("POSTULANTE")
                .build();

        when(usuarioRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(usuarioRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordHasher.hash(request.getPassword())).thenReturn("hashed_password");
        when(rolRepository.findByNombre("POSTULANTE")).thenReturn(Optional.of(usuarioEntity.getRol()));
        when(usuarioMapper.toEntity(request)).thenReturn(usuarioEntity);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioEntity);
        when(usuarioMapper.toDTO(usuarioEntity)).thenReturn(dto);

        UsuarioDTO result = usuarioServiceImpl.crearUsuario(request);

        assertNotNull(result);
        assertEquals("nuevo@test.com", result.getEmail());
        assertEquals("POSTULANTE",  result.getRol());
        assertEquals("nuevo",  result.getUsername());

        // Verificar que se haya hasheado la password antes de guardar
        verify(passwordHasher).hash(request.getPassword());
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void actualizarUsuarioDebeFallarSiNoExiste() {
        UsuarioUpdateRequest request = new UsuarioUpdateRequest();
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> usuarioServiceImpl.actualizarUsuario(99L, request));
    }

    @Test
    void obtenerUsuarioPorIdDebeRetornarDTO() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("user")
                .email("test@test.com")
                .rol(Rol.builder().nombre("POSTULANTE").build())
                .build();

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(1L)
                .username("user")
                .email("test@test.com")
                .rol("POSTULANTE")
                .build();

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(dto);

        UsuarioDTO result = usuarioServiceImpl.obtenerUsuarioPorId(1L);

        assertNotNull(result);
        assertEquals("test@test.com", result.getEmail());
        assertEquals("POSTULANTE", result.getRol());
    }

    @Test
    void obtenerUsuarioPorIdDebeFallarSiNoExiste() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> usuarioServiceImpl.obtenerUsuarioPorId(1L));
    }

    @Test
    void listarUsuariosDebeRetornarListaDeDTO() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .username("user")
                .email("test@test.com")
                .rol(Rol.builder().nombre("POSTULANTE").build())
                .build();

        UsuarioDTO dto = UsuarioDTO.builder()
                .id(1L)
                .username("user")
                .email("test@test.com")
                .rol("POSTULANTE")
                .build();

        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));
        when(usuarioMapper.toDTO(usuario)).thenReturn(dto);

        List<UsuarioDTO> result = usuarioServiceImpl.listarUsuarios();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test@test.com", result.get(0).getEmail());
    }

    @Test
    void eliminarUsuarioDebeEliminarCuandoExiste() {
        long id = 5L;
        when(usuarioRepository.existsById(id)).thenReturn(true);

        usuarioServiceImpl.eliminarUsuario(id);

        // Verificamos que se llamó a deleteById con el id correcto
        verify(usuarioRepository).deleteById(id);
    }

    @Test
    void eliminarUsuarioDebeFallarSiNoExiste() {
        long id = 9L;
        when(usuarioRepository.existsById(id)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> usuarioServiceImpl.eliminarUsuario(id));

        // Aseguramos que no se intentó borrar
        verify(usuarioRepository, never()).deleteById(anyLong());
    }
}
