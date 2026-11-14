package com.ejemplo.reservas.reserva;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    /**
     * ¿Hay alguna reserva activa en la sala que se solape con [inicio, fin)?
     *
     * Dos reservas se solapan si una empieza antes de que acabe la otra y viceversa.
     * Una reserva de 10:00 a 11:00 y otra de 11:00 a 12:00 NO se solapan.
     */
    @Query("""
            SELECT COUNT(r) > 0 FROM Reserva r
            WHERE r.sala.id = :salaId
              AND r.cancelada = false
              AND r.inicio < :fin
              AND r.fin > :inicio
            """)
    boolean haySolapamiento(@Param("salaId") Long salaId,
                            @Param("inicio") LocalDateTime inicio,
                            @Param("fin") LocalDateTime fin);

    /**
     * Listado con filtros opcionales. Si un filtro llega a null, no se aplica.
     *
     * El filtro por fecha devuelve las reservas que ocupan algún momento de ese día,
     * es decir, las que se solapan con [desde, hasta).
     */
    @Query("""
            SELECT r FROM Reserva r
            WHERE (:salaId IS NULL OR r.sala.id = :salaId)
              AND (:desde IS NULL OR (r.inicio < :hasta AND r.fin > :desde))
            """)
    Page<Reserva> buscar(@Param("salaId") Long salaId,
                         @Param("desde") LocalDateTime desde,
                         @Param("hasta") LocalDateTime hasta,
                         Pageable pageable);
}
