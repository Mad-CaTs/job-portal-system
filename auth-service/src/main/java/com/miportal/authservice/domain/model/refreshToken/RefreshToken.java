package com.miportal.authservice.domain.model.refreshToken;

import com.miportal.authservice.domain.model.BaseEntity;
import com.miportal.authservice.domain.model.usuario.Usuario;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tbl_refresh_token")
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "int_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "int_id_fk_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "vch_token", nullable = false, length = 500)
    private String token;

    @Column(name = "dt_expiracion", nullable = false)
    private LocalDateTime expiracion;
}