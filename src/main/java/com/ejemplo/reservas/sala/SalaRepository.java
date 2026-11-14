package com.ejemplo.reservas.sala;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalaRepository extends JpaRepository<Sala, Long> {

    boolean existsByNombreIgnoreCase(String nombre);
}
