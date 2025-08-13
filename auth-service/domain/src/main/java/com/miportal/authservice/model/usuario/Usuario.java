package com.miportal.authservice.model.usuario;

import com.miportal.authservice.model.BaseEntity;
import com.miportal.authservice.model.rol.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "TBL_USUARIO")
@Getter
@Setter
public class Usuario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "int_id")
    private Long id;

    @Column(name = "vch_username", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "vch_email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "vch_password", nullable = false, length = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "vch_tipo_usuario", nullable = false, length = 50)
    private TipoUsuario tipoUsuario;

    @Column(name = "bit_estado")
    private Boolean estado = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "int_id_fk_rol", nullable = false)
    private Rol roles;
}