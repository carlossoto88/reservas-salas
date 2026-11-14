package com.ejemplo.reservas.error;

import java.util.Map;

/**
 * Formato común de todas las respuestas de error.
 *
 * Ejemplo:
 * {
 *   "estado": 400,
 *   "mensaje": "Hay datos no válidos",
 *   "errores": { "capacidad": "La capacidad debe ser al menos 1" }
 * }
 */
public record ErrorResponse(int estado, String mensaje, Map<String, String> errores) {

    public ErrorResponse(int estado, String mensaje) {
        this(estado, mensaje, Map.of());
    }
}
