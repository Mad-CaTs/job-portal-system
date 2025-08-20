package com.miportal.authservice.application.dto.mapper;

import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.domain.model.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    @Mapping(source = "rol.nombre", target = "rol")
    UsuarioDTO toDTO(Usuario usuario);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estado", constant = "true")
    @Mapping(target = "usuarioCreacion", constant = "system")
    @Mapping(target = "rol", ignore = true)
    @Mapping(target = "password", ignore = true)
    Usuario toEntity(UsuarioCreateRequest request);
}
