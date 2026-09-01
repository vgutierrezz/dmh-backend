-- Inicialización de tabla Rol
INSERT INTO rol (id, name) VALUES
(1, 'ADMIN'),
(2, 'USER'),
(3, 'MODERATOR'),
(4, 'GUEST');

-- Si la tabla no existe, crear la tabla
CREATE TABLE IF NOT EXISTS rol (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

