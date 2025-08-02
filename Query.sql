Create Database DB_AuthService
use DB_AuthService

create table TBL_USUARIO
(
int_id INT IDENTITY(1,1) PRIMARY KEY,
vch_username VARCHAR(100) NOT NULL, 
vch_email VARCHAR(150) NOT NULL UNIQUE,
vch_password VARCHAR(255) NOT NULL,
vch_tipo_usuario VARCHAR(50) NOT NULL,
bit_estado BIT DEFAULT 1,

vch_usuario_creacion VARCHAR(100) NOT NULL,
dt_fec_creacion DATETIME NOT NULL DEFAULT GETDATE(),
vch_usuario_modificacion VARCHAR(100),
dt_fec_modificacion DATETIME,
)
GO

create table TBL_ROL
(
int_id INT IDENTITY(1,1) PRIMARY KEY,
vch_nombre VARCHAR(50) NOT NULL UNIQUE,
bit_estado BIT DEFAULT 1,

vch_usuario_creacion VARCHAR(100) NOT NULL,
dt_fec_creacion DATETIME NOT NULL DEFAULT GETDATE(),
vch_usuario_modificacion VARCHAR(100),
dt_fec_modificacion DATETIME,
)
GO

create table TBL_USUARIO_ROL
(
int_id_fk_usuario INT NOT NULL,
int_id_fk_rol INT NOT NULL,

FOREIGN KEY(int_id_fk_usuario) REFERENCES TBL_USUARIO(int_id),
FOREIGN KEY(int_id_fk_rol) REFERENCES TBL_ROL(int_id),

PRIMARY KEY (int_id_fk_usuario, int_id_fk_rol)
)
GO

create table TBL_REFRESH_TOKEN
(
int_id INT IDENTITY(1,1) PRIMARY KEY,
int_id_fk_usuario INT NOT NULL,
vch_token VARCHAR(500) NOT NULL,
dt_expiracion DATETIME NOT NULL,

FOREIGN KEY (int_id_fk_usuario) REFERENCES TBL_USUARIO(int_id),

vch_usuario_creacion VARCHAR(100) NOT NULL,
dt_fec_creacion DATETIME NOT NULL DEFAULT GETDATE(),
)
GO


