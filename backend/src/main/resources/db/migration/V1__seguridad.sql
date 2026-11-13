-- =====================================================================
-- V1__seguridad.sql  |  MySQL 8
-- Tablas del modulo de seguridad (sin restaurante todavia).
-- usuario.restaurante_id se agrega en V2, cuando exista la tabla restaurante.
-- =====================================================================

CREATE TABLE usuario (
    usuario_id    BIGINT       NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    telefono      VARCHAR(30)  NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id),
    CONSTRAINT uq_usuario_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE rol (
    rol_id      BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(60)  NOT NULL,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (rol_id),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE permiso (
    permiso_id  BIGINT       NOT NULL AUTO_INCREMENT,
    codigo      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NULL,
    PRIMARY KEY (permiso_id),
    CONSTRAINT uq_permiso_codigo UNIQUE (codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE usuario_rol (
    usuario_id  BIGINT   NOT NULL,
    rol_id      BIGINT   NOT NULL,
    asignado_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (usuario_id),
    CONSTRAINT fk_usuario_rol_rol     FOREIGN KEY (rol_id)     REFERENCES rol (rol_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE rol_permiso (
    rol_id      BIGINT   NOT NULL,
    permiso_id  BIGINT   NOT NULL,
    asignado_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (rol_id, permiso_id),
    CONSTRAINT fk_rol_permiso_rol     FOREIGN KEY (rol_id)     REFERENCES rol (rol_id),
    CONSTRAINT fk_rol_permiso_permiso FOREIGN KEY (permiso_id) REFERENCES permiso (permiso_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Datos base
INSERT INTO rol (nombre, descripcion) VALUES
  ('ADMIN',       'Administrador de la plataforma'),
  ('RESTAURANTE', 'Dueno o personal de un restaurante'),
  ('CLIENTE',     'Cliente que realiza pedidos'),
  ('REPARTIDOR',  'Encargado de las entregas');

INSERT INTO permiso (codigo, descripcion) VALUES
  ('USUARIO_ADMIN', 'Administrar usuarios y roles');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.rol_id, p.permiso_id
FROM rol r JOIN permiso p ON p.codigo = 'USUARIO_ADMIN'
WHERE r.nombre = 'ADMIN';
