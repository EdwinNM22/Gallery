package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Expediente;
import com.proyecto.galeria.model.album;
import com.proyecto.galeria.model.usuario;

import java.util.List;
import java.util.Optional;

public interface ExpedienteService {

    Expediente save(Expediente Expediente);
    Optional<Expediente> get(Integer id);
    void update(Expediente Expediente);
    void delete(Integer id);
    List<Expediente> findAll();
    Expediente findById(Integer id);
    List<Expediente> findByUsuario(usuario usuario); // nuevo método
    void deleteById(Integer id);


}
