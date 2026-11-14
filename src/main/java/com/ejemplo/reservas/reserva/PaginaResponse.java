package com.ejemplo.reservas.reserva;

import org.springframework.data.domain.Page;

import java.util.List;

/** Una página de resultados junto con los datos para pedir la siguiente. */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas
) {

    public static <T> PaginaResponse<T> desde(Page<T> page) {
        return new PaginaResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }
}
