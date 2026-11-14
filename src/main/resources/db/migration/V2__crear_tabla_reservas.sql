CREATE TABLE reservas (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    sala_id     BIGINT       NOT NULL,
    responsable VARCHAR(100) NOT NULL,
    inicio      TIMESTAMP    NOT NULL,
    fin         TIMESTAMP    NOT NULL,
    cancelada   BOOLEAN      NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_reservas_sala FOREIGN KEY (sala_id) REFERENCES salas (id)
);

-- Acelera la búsqueda de reservas de una sala por horario
CREATE INDEX idx_reservas_sala_inicio ON reservas (sala_id, inicio);
