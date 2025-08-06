package com.miportal.authservice.model.refreshToken;

import com.miportal.authservice.model.BaseEntity;
import com.miportal.authservice.model.usuario.Usuario;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_REFRESH_TOKEN")
@Getter
@Setter
public class RefreshToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "int_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "int_id_fk_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "vch_token", nullable = false, length = 500)
    private String token;

    @Column(name = "dt_expiracion", nullable = false)
    private LocalDateTime expiracion;
}