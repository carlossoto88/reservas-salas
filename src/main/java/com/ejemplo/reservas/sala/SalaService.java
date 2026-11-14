package com.ejemplo.reservas.sala;

import com.ejemplo.reservas.error.ConflictoException;
import com.ejemplo.reservas.error.NoEncontradoException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public Sala crear(CrearSalaRequest request) {
        String nombre = request.nombre().trim();

        if (salaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictoException("Ya existe una sala con el nombre '" + nombre + "'");
        }

        return salaRepository.save(new Sala(nombre, request.capacidad()));
    }

    public List<Sala> listar() {
        return salaRepository.findAll(Sort.by("nombre"));
    }

    public Sala buscarPorId(Long id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new NoEncontradoException("No existe la sala con id " + id));
    }
}
