package com.miportal.auth_service.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Column(name="vch_usuario_creacion", nullable = false, length = 100, updatable = false)
    private String usuarioCreacion;

    @Column(name="dt_fec_creacion", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime fechaCreacion;

    @Column(name="vch_usuario_modificacion", length = 100)
    private String usuarioModificacion;

    @Column(name="vch_usuario_modificacion")
    @CreationTimestamp
    private LocalDateTime fechaModificacion;
}
