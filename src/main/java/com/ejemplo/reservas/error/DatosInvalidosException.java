package com.ejemplo.reservas.error;

/** Error de validación que no se puede expresar con anotaciones. Se traduce a un 400. */
public class DatosInvalidosException extends RuntimeException {

    private final String campo;

    public DatosInvalidosException(String campo, String mensaje) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
