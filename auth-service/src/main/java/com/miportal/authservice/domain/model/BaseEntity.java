package com.miportal.authservice.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract class BaseEntity {

    @CreatedBy
    @Column(name = "vch_usuario_creacion", nullable = false, updatable = false)
    private String usuarioCreacion;

    @CreatedDate
    @Column(name = "dt_fec_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedBy
    @Column(name = "vch_usuario_modificacion", insertable = false)
    private String usuarioModificacion;

    @LastModifiedDate
    @Column(name = "dt_fec_modificacion", insertable = false)
    private LocalDateTime fechaModificacion;

    @PreUpdate
    public void preUpdate() {
        if (fechaModificacion == null) {
            fechaModificacion = LocalDateTime.now();
        }
    }
}