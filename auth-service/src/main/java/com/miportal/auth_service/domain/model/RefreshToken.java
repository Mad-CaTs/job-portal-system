package com.miportal.auth_service.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TBL_REFRESH_TOKEN")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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