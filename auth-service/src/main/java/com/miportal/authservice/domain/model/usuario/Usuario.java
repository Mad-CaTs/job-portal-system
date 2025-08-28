package com.miportal.authservice.domain.model.usuario;

import com.miportal.authservice.domain.model.BaseEntity;
import com.miportal.authservice.domain.model.rol.Rol;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "TBL_USUARIO", schema = "dbo")
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

    @Column(name = "bit_estado")
    private Boolean estado = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "int_id_fk_rol", nullable = false)
    private Rol rol;
}