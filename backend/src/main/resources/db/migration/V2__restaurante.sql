-- =====================================================================
-- V2__restaurante.sql  |  MySQL 8
-- Agrega la tabla restaurante y vincula usuario -> restaurante.
-- Ejemplo real de "evolucionar" la BD sin tocar V1.
-- =====================================================================

CREATE TABLE restaurante (
    restaurante_id BIGINT       NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(150) NOT NULL,
    direccion      VARCHAR(255) NULL,
    telefono       VARCHAR(30)  NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    creado_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (restaurante_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE usuario
    ADD COLUMN restaurante_id BIGINT NULL,
    ADD CONSTRAINT fk_usuario_restaurante
        FOREIGN KEY (restaurante_id) REFERENCES restaurante (restaurante_id);

-- Permisos nuevos de este modulo
INSERT INTO permiso (codigo, descripcion) VALUES
  ('RESTAURANTE_LEER',     'Ver datos del restaurante'),
  ('RESTAURANTE_ESCRIBIR', 'Editar datos del restaurante');

INSERT INTO rol_permiso (rol_id, permiso_id)
SELECT r.rol_id, p.permiso_id
FROM rol r JOIN permiso p ON p.codigo IN ('RESTAURANTE_LEER', 'RESTAURANTE_ESCRIBIR')
WHERE r.nombre IN ('ADMIN', 'RESTAURANTE');
