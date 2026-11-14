package com.ejemplo.reservas.reserva;

import com.ejemplo.reservas.error.ConflictoException;
import com.ejemplo.reservas.error.DatosInvalidosException;
import com.ejemplo.reservas.error.NoEncontradoException;
import com.ejemplo.reservas.sala.Sala;
import com.ejemplo.reservas.sala.SalaService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalaService salaService;

    public ReservaService(ReservaRepository reservaRepository, SalaService salaService) {
        this.reservaRepository = reservaRepository;
        this.salaService = salaService;
    }

    @Transactional
    public Reserva crear(CrearReservaRequest request) {
        // 1. El fin tiene que ser posterior al inicio
        if (!request.fin().isAfter(request.inicio())) {
            throw new DatosInvalidosException("fin", "La fecha de fin tiene que ser posterior a la de inicio");
        }

        // 2. La sala tiene que existir (si no, 404)
        Sala sala = salaService.buscarPorId(request.salaId());

        // 3. La sala no puede estar ocupada en ese horario (si lo está, 409)
        if (reservaRepository.haySolapamiento(sala.getId(), request.inicio(), request.fin())) {
            throw new ConflictoException("La sala '" + sala.getNombre() + "' ya está ocupada en ese horario");
        }

        Reserva reserva = new Reserva(sala, request.responsable().trim(), request.inicio(), request.fin());
        return reservaRepository.save(reserva);
    }

    /**
     * Lista reservas paginadas, ordenadas por fecha de inicio.
     *
     * @param salaId  si no es null, solo las de esa sala
     * @param fecha   si no es null, solo las que ocupan algún momento de ese día
     * @param pagina  número de página, empezando en 0
     * @param tamano  elementos por página (entre 1 y 100)
     */
    public Page<Reserva> listar(Long salaId, LocalDate fecha, int pagina, int tamano) {
        if (pagina < 0) {
            throw new DatosInvalidosException("pagina", "La página no puede ser negativa");
        }
        if (tamano < 1 || tamano > 100) {
            throw new DatosInvalidosException("tamano", "El tamaño de página debe estar entre 1 y 100");
        }

        LocalDateTime desde = null;
        LocalDateTime hasta = null;
        if (fecha != null) {
            desde = fecha.atStartOfDay();           // 00:00 de ese día
            hasta = fecha.plusDays(1).atStartOfDay(); // 00:00 del día siguiente
        }

        Pageable pageable = PageRequest.of(pagina, tamano, Sort.by("inicio"));
        return reservaRepository.buscar(salaId, desde, hasta, pageable);
    }

    @Transactional
    public void cancelar(Long id) {
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la reserva con id " + id));

        if (reserva.isCancelada()) {
            throw new ConflictoException("La reserva " + id + " ya estaba cancelada");
        }

        reserva.cancelar();
    }
}
