package com.ejemplo.reservas.reserva;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/** Datos que llegan en el POST para crear una reserva. */
public record CrearReservaRequest(

        @NotNull(message = "La sala es obligatoria")
        Long salaId,

        @NotBlank(message = "El responsable es obligatorio")
        @Size(max = 100, message = "El responsable no puede tener más de 100 caracteres")
        String responsable,

        @NotNull(message = "La fecha de inicio es obligatoria")
        @Future(message = "La fecha de inicio tiene que ser futura")
        LocalDateTime inicio,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime fin
) {
}
