package com.ejemplo.reservas.sala;

/** Lo que devuelve la API cuando se consulta una sala. */
public record SalaResponse(Long id, String nombre, int capacidad) {

    public static SalaResponse desde(Sala sala) {
        return new SalaResponse(sala.getId(), sala.getNombre(), sala.getCapacidad());
    }
}
