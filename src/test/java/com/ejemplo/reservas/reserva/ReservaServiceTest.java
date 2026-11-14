package com.ejemplo.reservas.reserva;

import com.ejemplo.reservas.error.ConflictoException;
import com.ejemplo.reservas.error.DatosInvalidosException;
import com.ejemplo.reservas.sala.Sala;
import com.ejemplo.reservas.sala.SalaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios del servicio de reservas.
 * No arrancan Spring ni base de datos: el repositorio y el servicio de salas son mocks.
 */
class ReservaServiceTest {

    private static final LocalDateTime MANANA_10H = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);

    private ReservaRepository reservaRepository;
    private SalaService salaService;
    private ReservaService reservaService;

    @BeforeEach
    void preparar() {
        reservaRepository = mock(ReservaRepository.class);
        salaService = mock(SalaService.class);
        reservaService = new ReservaService(reservaRepository, salaService);

        when(salaService.buscarPorId(1L)).thenReturn(new Sala("Sala Azul", 8));
    }

    @Test
    void creaLaReservaSiLaSalaEstaLibre() {
        when(reservaRepository.haySolapamiento(any(), any(), any())).thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva reserva = reservaService.crear(
                new CrearReservaRequest(1L, "  Ana  ", MANANA_10H, MANANA_10H.plusHours(1)));

        assertThat(reserva.getResponsable()).isEqualTo("Ana");
        assertThat(reserva.isCancelada()).isFalse();
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void lanzaConflictoSiLaSalaYaEstaOcupada() {
        when(reservaRepository.haySolapamiento(any(), any(), any())).thenReturn(true);

        CrearReservaRequest request = new CrearReservaRequest(1L, "Ana", MANANA_10H, MANANA_10H.plusHours(1));

        assertThatThrownBy(() -> reservaService.crear(request))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("ya está ocupada");
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void lanzaErrorSiElFinNoEsPosteriorAlInicio() {
        CrearReservaRequest request = new CrearReservaRequest(1L, "Ana", MANANA_10H, MANANA_10H.minusHours(1));

        assertThatThrownBy(() -> reservaService.crear(request))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("posterior");
        verify(reservaRepository, never()).save(any());
    }
}
