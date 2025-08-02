package com.proyecto.galeria.service;

import java.util.List;
import java.util.Optional;

import com.proyecto.galeria.model.AdvertenciaTipo;

public interface AdvertenciaTipoService {
    List<AdvertenciaTipo> findAll();

    Optional<AdvertenciaTipo> findById(Integer id);

    AdvertenciaTipo save(AdvertenciaTipo usuarioAdvertencia);

    AdvertenciaTipo update(Integer id, AdvertenciaTipo newAdvertenciaTipo);

    void delete(Integer id);

    Optional<AdvertenciaTipo> findByName(String name);
}