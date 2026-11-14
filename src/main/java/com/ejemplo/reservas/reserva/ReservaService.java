package com.ejemplo.reservas.reserva;

import com.ejemplo.reservas.error.ConflictoException;
import com.ejemplo.reservas.error.DatosInvalidosException;
import com.ejemplo.reservas.error.NoEncontradoException;
import com.ejemplo.reservas.sala.Sala;
import com.ejemplo.reservas.sala.SalaService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    public List<Reserva> listar() {
        return reservaRepository.findAll(Sort.by("inicio"));
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
