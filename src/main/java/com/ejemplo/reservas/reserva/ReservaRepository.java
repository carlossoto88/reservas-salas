package com.ejemplo.reservas.reserva;

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
}
