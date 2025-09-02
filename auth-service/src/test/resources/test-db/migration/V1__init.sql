-- ROLES
CREATE TABLE [dbo].[tbl_rol] (
    int_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    vch_nombre VARCHAR(50) NOT NULL UNIQUE,
    bit_estado BIT DEFAULT 1,

    vch_usuario_creacion VARCHAR(100) NOT NULL,
    dt_fec_creacion DATETIME NOT NULL DEFAULT GETDATE(),
    vch_usuario_modificacion VARCHAR(100),
    dt_fec_modificacion DATETIME
);

-- Insertar roles fijos
INSERT INTO [dbo].[tbl_rol] (vch_nombre, vch_usuario_creacion)
VALUES
    ('POSTULANTE', 'system'),
    ('EMPRESA', 'system'),
    ('ADMIN', 'system');

-- USUARIOS
CREATE TABLE [dbo].[tbl_usuario] (
    int_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    vch_username VARCHAR(100) NOT NULL UNIQUE,
    vch_email VARCHAR(150) NOT NULL UNIQUE,
    vch_password VARCHAR(255) NOT NULL,
    bit_estado BIT DEFAULT 1,

    int_id_fk_rol BIGINT NOT NULL,
    CONSTRAINT FK_USUARIO_ROL FOREIGN KEY (int_id_fk_rol) REFERENCES tbl_rol(int_id),

    vch_usuario_creacion VARCHAR(100) NOT NULL,
    dt_fec_creacion DATETIME NOT NULL DEFAULT GETDATE(),
    vch_usuario_modificacion VARCHAR(100),
    dt_fec_modificacion DATETIME
);

-- REFRESH TOKENS
CREATE TABLE [dbo].[tbl_refresh_token] (
    int_id BIGINT IDENTITY(1,1) PRIMARY KEY,
    int_id_fk_usuario BIGINT NOT NULL,
    CONSTRAINT FK_REFRESH_USUARIO FOREIGN KEY (int_id_fk_usuario) REFERENCES tbl_usuario(int_id),
    vch_token VARCHAR(500) NOT NULL,
    dt_expiracion DATETIME NOT NULL,

    vch_usuario_creacion VARCHAR(100) NOT NULL,
    dt_fec_creacion DATETIME NOT NULL DEFAULT GETDATE()
);
