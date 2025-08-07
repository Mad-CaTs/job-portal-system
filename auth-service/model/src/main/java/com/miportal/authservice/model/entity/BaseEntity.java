package com.miportal.authservice.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {

    @Column(name = "vch_usuario_creacion", nullable = false)
    private String usuarioCreacion;

    @Column(name = "dt_fec_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "vch_usuario_modificacion")
    private String usuarioModificacion;

    @Column(name = "dt_fec_modificacion")
    private LocalDateTime fechaModificacion;

    @PrePersist
    public void prePersist() {
        fechaCreacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}