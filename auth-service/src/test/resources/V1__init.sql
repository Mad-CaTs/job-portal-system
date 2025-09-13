-- Insertar roles para tests

INSERT INTO tbl_rol (vch_nombre, vch_usuario_creacion, dt_fec_creacion)
VALUES
    ('POSTULANTE', 'system', CURRENT_TIMESTAMP),
    ('EMPRESA', 'system', CURRENT_TIMESTAMP),
    ('ADMIN', 'system', CURRENT_TIMESTAMP);