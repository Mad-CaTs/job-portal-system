package com.miportal.authservice.model.dto.rol;

import java.util.List;
import java.util.UUID;

public class RolDTO {
    private UUID id;
    private String nombre;
    private List<String> permisos;
}
