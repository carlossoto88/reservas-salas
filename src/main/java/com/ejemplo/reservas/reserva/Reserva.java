package com.ejemplo.reservas.reserva;

import com.ejemplo.reservas.sala.Sala;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sala_id")
    private Sala sala;

    private String responsable;

    private LocalDateTime inicio;

    private LocalDateTime fin;

    // Las reservas no se borran: se marcan como canceladas para no perder el histórico
    private boolean cancelada;

    // Constructor vacío que necesita JPA
    protected Reserva() {
    }

    public Reserva(Sala sala, String responsable, LocalDateTime inicio, LocalDateTime fin) {
        this.sala = sala;
        this.responsable = responsable;
        this.inicio = inicio;
        this.fin = fin;
        this.cancelada = false;
    }

    public void cancelar() {
        this.cancelada = true;
    }

    public Long getId() {
        return id;
    }

    public Sala getSala() {
        return sala;
    }

    public String getResponsable() {
        return responsable;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public LocalDateTime getFin() {
        return fin;
    }

    public boolean isCancelada() {
        return cancelada;
    }
}
