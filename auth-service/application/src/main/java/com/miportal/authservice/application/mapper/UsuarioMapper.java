package com.miportal.authservice.application.mapper;

import com.miportal.authservice.application.dto.usuario.UsuarioCreateRequest;
import com.miportal.authservice.application.dto.usuario.UsuarioDTO;
import com.miportal.authservice.model.usuario.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioDTO toDTO(Usuario usuario);
    List<UsuarioDTO> toDTOList(List<Usuario> usuarios);

    @Mapping(target = "id", ignore = true) //Ignorar id porque la bd lo autogenera
    @Mapping(target = "estado", constant = "true")
    Usuario toEntity(UsuarioCreateRequest request);
}
