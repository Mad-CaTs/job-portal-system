CREATE DATABASE DB_AuthService;

-- ROLES
CREATE TABLE tbl_rol (
    int_id BIGSERIAL PRIMARY KEY,
    vch_nombre VARCHAR(50) NOT NULL UNIQUE,
    bit_estado BOOLEAN DEFAULT TRUE,

    vch_usuario_creacion VARCHAR(100) NOT NULL,
    dt_fec_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vch_usuario_modificacion VARCHAR(100),
    dt_fec_modificacion TIMESTAMP
);

-- Insertar roles fijos
INSERT INTO tbl_rol (vch_nombre, vch_usuario_creacion)
VALUES
    ('POSTULANTE', 'system'),
    ('EMPRESA', 'system'),
    ('ADMIN', 'system');

-- USUARIOS
CREATE TABLE tbl_usuario (
    int_id BIGSERIAL PRIMARY KEY,
    vch_username VARCHAR(100) NOT NULL UNIQUE,
    vch_email VARCHAR(150) NOT NULL UNIQUE,
    vch_password VARCHAR(255) NOT NULL,
    bit_estado BOOLEAN DEFAULT TRUE,

    int_id_fk_rol BIGINT NOT NULL,
    CONSTRAINT fk_usuario_rol FOREIGN KEY (int_id_fk_rol) REFERENCES tbl_rol(int_id),

    vch_usuario_creacion VARCHAR(100) NOT NULL,
    dt_fec_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vch_usuario_modificacion VARCHAR(100),
    dt_fec_modificacion TIMESTAMP
);

-- REFRESH TOKENS
CREATE TABLE tbl_refresh_token (
    int_id BIGSERIAL PRIMARY KEY,
    int_id_fk_usuario BIGINT NOT NULL,
    CONSTRAINT fk_refresh_usuario FOREIGN KEY (int_id_fk_usuario) REFERENCES tbl_usuario(int_id),
    vch_token VARCHAR(500) NOT NULL,
    dt_expiracion TIMESTAMP NOT NULL,

    vch_usuario_creacion VARCHAR(100) NOT NULL,
    dt_fec_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vch_usuario_modificacion VARCHAR(100),
    dt_fec_modificacion TIMESTAMP
);

select * from tbl_usuario;
select * from tbl_rol;
select * from tbl_refresh_token;
