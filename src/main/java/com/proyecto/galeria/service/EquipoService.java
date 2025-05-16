package com.proyecto.galeria.service;

import com.proyecto.galeria.model.Equipo;

import java.util.List;
import java.util.Optional;

public interface EquipoService {
    List<Equipo> findAll();
    List<Equipo> findByProyectoId(Integer proyectoId);
    Equipo save(Equipo equipo);
    Optional<Equipo> findById(Integer id);
    void delete(Integer id);
    boolean existsById(Integer id);
}