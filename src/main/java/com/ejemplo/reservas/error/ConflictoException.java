package com.ejemplo.reservas.error;

/** Se lanza cuando la operación choca con el estado actual (p. ej. sala ocupada). Se traduce a un 409. */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
