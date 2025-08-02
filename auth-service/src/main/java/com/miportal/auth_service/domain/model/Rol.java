package com.miportal.auth_service.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;
@Entity
@Table(name = "TBL_ROL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "int_id")
    private Long id;

    @Column(name = "vch_nombre", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "bit_estado", nullable = false)
    private Boolean estado;

    @ManyToMany(mappedBy = "roles")
    private Set<Usuario> usuarios;
}
