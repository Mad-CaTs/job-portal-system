package com.miportal.auth_service.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name="TBL_USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "int_id")
    private Long id;

    @Column(name = "vch_username", nullable = false, length = 100)
    private String username;

    @Column(name = "vch_email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "vch_password", nullable = false, length = 255)
    private String password;

    @Column(name = "vch_tipo_usuario", nullable = false, length = 50)
    private String tipoUsuario;

    @Column(name = "bit_estado", nullable = false)
    private Boolean estado;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "TBL_USUARIO_ROL",
            joinColumns = @JoinColumn(name = "int_id_fk_usuario"),
            inverseJoinColumns = @JoinColumn(name = "int_id_fk_rol")
    )
    private Set<Rol> roles;
}
