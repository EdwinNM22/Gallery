package com.proyecto.galeria.service.Impl;

import com.proyecto.galeria.model.Expediente;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.usuario;
import com.proyecto.galeria.repository.ExpedienteRepository;
import com.proyecto.galeria.service.ExpedienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ExpedienteImpl implements ExpedienteService {


    @Autowired
    private ExpedienteRepository expedienteRepository;

    @Override
    public Expediente save(Expediente expediente) {
        return expedienteRepository.save(expediente);
    }

    @Override
    public Optional<Expediente> get(Integer id) {
        return expedienteRepository.findById(id);
    }

    @Override
    public void update(Expediente Expediente) {
        expedienteRepository.save(Expediente);

    }

    @Override
    public void delete(Integer id) {
        expedienteRepository.deleteById(id);

    }

    @Override
    public List<Expediente> findAll() {
        return expedienteRepository.findAll();
    }

    @Override
    public Expediente findById(Integer id) {
        return expedienteRepository.findById(id).get();
    }

    @Override
    public List<Expediente> findByUsuario(usuario usuario) {
        return expedienteRepository.findByUsuario(usuario);
    }

    @Override
    public void deleteById(Integer id) {
        expedienteRepository.deleteById(id);
    }
}
