package com.ejemplo.reservas.error;

/** Se lanza cuando no existe el recurso pedido. Se traduce a un 404. */
public class NoEncontradoException extends RuntimeException {

    public NoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
