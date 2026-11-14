package com.ejemplo.reservas.reserva;

import java.time.LocalDateTime;

/** Lo que devuelve la API cuando se consulta una reserva. */
public record ReservaResponse(
        Long id,
        Long salaId,
        String nombreSala,
        String responsable,
        LocalDateTime inicio,
        LocalDateTime fin,
        boolean cancelada
) {

    public static ReservaResponse desde(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getSala().getId(),
                reserva.getSala().getNombre(),
                reserva.getResponsable(),
                reserva.getInicio(),
                reserva.getFin(),
                reserva.isCancelada());
    }
}
