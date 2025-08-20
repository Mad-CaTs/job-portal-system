package com.miportal.authservice.domain.model.rol;

import com.miportal.authservice.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "TBL_ROL")
@Getter
@Setter
public class Rol extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "int_id")
    private Long id;

    @Column(name = "vch_nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "bit_estado")
    private Boolean estado = true;
}