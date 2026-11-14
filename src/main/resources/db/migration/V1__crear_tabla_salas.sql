CREATE TABLE salas (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL UNIQUE,
    capacidad INT          NOT NULL
);
